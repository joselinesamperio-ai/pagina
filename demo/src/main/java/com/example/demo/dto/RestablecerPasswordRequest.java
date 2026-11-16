package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequest(
        @NotBlank @Size(max = 200) String codigo,
        @NotBlank @Size(min = 8, max = 72) String passwordNueva
) {
}
