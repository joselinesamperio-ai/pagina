package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Códigos de recuperación de contraseña: aleatorios de 256 bits, de UN SOLO USO y con caducidad de
 * 15 minutos. En memoria solo se guarda su hash SHA-256 (el código real solo viaja por correo).
 * Una persona tiene un único código vigente a la vez y no puede pedir otro durante 60 segundos.
 */
@Component
public class RecuperacionPassword {

    public static final Duration DURACION = Duration.ofMinutes(15);
    static final Duration ESPERA = Duration.ofSeconds(60);
    private static final int MAXIMO_VIGENTES = 10_000;

    private record Codigo(Long usuarioId, Instant expira, Instant emitido) {
    }

    private final Map<String, Codigo> porHash = new ConcurrentHashMap<>();
    private final Map<Long, String> hashDe = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock reloj;

    @Autowired
    public RecuperacionPassword() {
        this(Clock.systemUTC());
    }

    RecuperacionPassword(Clock reloj) {
        this.reloj = reloj;
    }

    /** Devuelve un código nuevo, o vacío si se pidió hace menos de 60 s o hay demasiados vigentes. */
    public synchronized Optional<String> emitir(Long usuarioId) {
        Instant ahora = reloj.instant();
        porHash.values().removeIf(c -> c.expira().isBefore(ahora));
        hashDe.values().removeIf(h -> !porHash.containsKey(h));

        String anterior = hashDe.get(usuarioId);
        if (anterior != null && porHash.get(anterior).emitido().plus(ESPERA).isAfter(ahora)) {
            return Optional.empty();
        }
        if (anterior == null && porHash.size() >= MAXIMO_VIGENTES) {
            return Optional.empty();
        }
        if (anterior != null) {
            porHash.remove(anterior);
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256(codigo);
        porHash.put(hash, new Codigo(usuarioId, ahora.plus(DURACION), ahora));
        hashDe.put(usuarioId, hash);
        return Optional.of(codigo);
    }

    /** Valida el código y lo invalida (un solo uso). Devuelve el id del usuario si era válido. */
    public synchronized Optional<Long> consumir(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        String hash = sha256(codigo.trim());
        Codigo c = porHash.remove(hash);
        if (c == null) {
            return Optional.empty();
        }
        hashDe.remove(c.usuarioId(), hash);
        if (c.expira().isBefore(reloj.instant())) {
            return Optional.empty();
        }
        return Optional.of(c.usuarioId());
    }

    /** Invalida el código vigente de una persona (por ejemplo, al cancelar su cuenta). */
    public synchronized void revocar(Long usuarioId) {
        String hash = hashDe.remove(usuarioId);
        if (hash != null) {
            porHash.remove(hash);
        }
    }

    private static String sha256(String texto) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
