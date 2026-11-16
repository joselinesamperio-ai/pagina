package com.example.demo.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Sesiones en memoria: token aleatorio de 256 bits con caducidad de 30 minutos. */
@Component
public class TokenService {

    public static final Duration DURACION = Duration.ofMinutes(30);

    private record Sesion(Long usuarioId, Instant expira) {
    }

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String crear(Long usuarioId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(usuarioId, Instant.now().plus(DURACION)));
        return token;
    }

    public Optional<Long> usuarioDe(String token) {
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (sesion.expira().isBefore(Instant.now())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion.usuarioId());
    }

    public void revocar(String token) {
        sesiones.remove(token);
    }

    public void revocarTodas(Long usuarioId) {
        sesiones.values().removeIf(s -> s.usuarioId().equals(usuarioId));
    }
}
