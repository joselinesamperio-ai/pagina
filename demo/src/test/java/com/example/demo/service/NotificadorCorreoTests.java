package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificadorCorreoTests {

    @Test
    void escapaTextoComoJson() {
        assertEquals("\"Ana \\\"la\\\" Pérez\\n\\\\\"", NotificadorCorreo.json("Ana \"la\" Pérez\n\\"));
    }

    @Test
    void sinConfiguracionQuedaDesactivadoYNoFalla() {
        NotificadorCorreo apagado = new NotificadorCorreo("", "", "Catálogo", "");
        assertFalse(apagado.activo());
        apagado.inicioDeSesion("Ana", "ana@correo.com");
        apagado.cuentaCreada("Ana", "ana@correo.com");
        assertTrue(new NotificadorCorreo("k", "a@b.com", "Catálogo", "").activo());
    }
}
