package com.example.demo.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

/** Hash de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria (sin dependencias extra). */
@Component
public class PasswordHasher {

    private static final int ITERACIONES = 210_000;
    private static final int BITS_CLAVE = 256;
    private static final int BYTES_SAL = 16;

    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        byte[] sal = new byte[BYTES_SAL];
        random.nextBytes(sal);
        byte[] derivada = derivar(password, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(derivada);
    }

    public boolean verifica(String password, String almacenado) {
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = derivar(password, sal, Integer.parseInt(partes[1]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    private byte[] derivar(String password, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sal, iteraciones, BITS_CLAVE);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
