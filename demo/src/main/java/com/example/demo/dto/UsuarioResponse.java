package com.example.demo.dto;

import com.example.demo.entity.Usuario;
import java.time.Instant;

/** Nunca incluye el hash de la contraseña. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String versionPolitica,
        Instant fechaConsentimiento,
        Instant fechaRegistro,
        boolean aceptaComunicaciones
) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getVersionPolitica(),
                u.getFechaConsentimiento(), u.getFechaRegistro(), u.isAceptaComunicaciones());
    }
}
