package com.example.demo.dto;

public record LoginResponse(String token, long expiraEnSegundos, UsuarioResponse usuario) {
}
