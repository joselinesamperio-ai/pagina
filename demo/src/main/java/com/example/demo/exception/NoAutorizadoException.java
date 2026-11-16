package com.example.demo.exception;

public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
