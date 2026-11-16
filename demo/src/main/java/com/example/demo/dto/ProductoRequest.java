package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precio,
        @NotNull Long categoriaId
) {
}
