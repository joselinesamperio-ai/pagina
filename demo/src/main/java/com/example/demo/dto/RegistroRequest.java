package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 120) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Boolean aceptaPoliticaPrivacidad,
        Boolean aceptaComunicaciones
) {
}
