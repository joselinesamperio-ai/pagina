package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Email @Size(max = 120) String correo,
        @Size(max = 20) String telefono,
        @Size(max = 200) String direccion
) {
}
