package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecuperacionPasswordTests {

    /** Reloj que se puede adelantar. */
    static class RelojManual extends Clock {
        Instant ahora = Instant.parse("2026-11-16T12:00:00Z");

        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId z) { return this; }
        @Override public Instant instant() { return ahora; }
        void avanza(Duration d) { ahora = ahora.plus(d); }
    }

    @Test
    void codigoEsDeUnSoloUso() {
        RecuperacionPassword r = new RecuperacionPassword(new RelojManual());
        String codigo = r.emitir(7L).orElseThrow();
        assertTrue(codigo.length() >= 40);
        assertEquals(Optional.of(7L), r.consumir(codigo));
        assertTrue(r.consumir(codigo).isEmpty());
    }

    @Test
    void codigoCaducaALos15Minutos() {
        RelojManual reloj = new RelojManual();
        RecuperacionPassword r = new RecuperacionPassword(reloj);
        String codigo = r.emitir(7L).orElseThrow();
        reloj.avanza(Duration.ofMinutes(16));
        assertTrue(r.consumir(codigo).isEmpty());
    }

    @Test
    void noPermitePedirOtroAntesDe60SegundosYElNuevoInvalidaAlAnterior() {
        RelojManual reloj = new RelojManual();
        RecuperacionPassword r = new RecuperacionPassword(reloj);
        String primero = r.emitir(7L).orElseThrow();
        assertTrue(r.emitir(7L).isEmpty());
        reloj.avanza(Duration.ofSeconds(61));
        String segundo = r.emitir(7L).orElseThrow();
        assertNotEquals(primero, segundo);
        assertTrue(r.consumir(primero).isEmpty());
        assertEquals(Optional.of(7L), r.consumir(segundo));
    }

    @Test
    void rechazaCodigosInventadosYVacios() {
        RecuperacionPassword r = new RecuperacionPassword(new RelojManual());
        r.emitir(7L);
        assertTrue(r.consumir("inventado").isEmpty());
        assertTrue(r.consumir("").isEmpty());
        assertTrue(r.consumir(null).isEmpty());
    }

    @Test
    void revocarInvalidaElCodigoVigente() {
        RecuperacionPassword r = new RecuperacionPassword(new RelojManual());
        String codigo = r.emitir(7L).orElseThrow();
        r.revocar(7L);
        assertTrue(r.consumir(codigo).isEmpty());
    }
}
