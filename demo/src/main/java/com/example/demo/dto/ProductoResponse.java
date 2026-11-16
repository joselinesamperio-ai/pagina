package com.example.demo.dto;

import com.example.demo.entity.Producto;
import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        BigDecimal precio,
        Long categoriaId,
        String categoriaNombre
) {

    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}
