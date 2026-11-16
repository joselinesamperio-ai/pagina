package com.example.demo.dto;

import com.example.demo.entity.Proveedor;

public record ProveedorResponse(Long id, String nombre, String correo, String telefono, String direccion) {

    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getNombre(), p.getCorreo(), p.getTelefono(), p.getDireccion());
    }
}
