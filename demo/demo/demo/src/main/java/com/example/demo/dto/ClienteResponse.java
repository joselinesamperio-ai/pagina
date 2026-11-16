package com.example.demo.dto;

import com.example.demo.entity.Cliente;

public record ClienteResponse(Long id, String nombre, String correo, String telefono) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getCorreo(), c.getTelefono());
    }
}
