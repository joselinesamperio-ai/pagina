package com.example.demo.service;

import java.util.List;
import java.util.Map;

/** Datos vigentes de la política de privacidad (ver docs/POLITICA_PRIVACIDAD.md). */
public final class PoliticaPrivacidad {

    public static final String VERSION = "1.0";

    private PoliticaPrivacidad() {
    }

    public static Map<String, Object> resumen() {
        return Map.of(
                "version", VERSION,
                "documento", "docs/POLITICA_PRIVACIDAD.md",
                "datosRecabados", List.of("nombre", "correo", "contraseña (solo hash)", "consentimiento"),
                "finalidadesNecesarias", List.of("administrar la cuenta", "autenticar", "atender derechos ARCO"),
                "finalidadesOpcionales", List.of("comunicaciones del proyecto (desactivadas por defecto)"),
                "derechosArco", Map.of(
                        "acceso", "GET /api/login/yo",
                        "rectificacion", "PUT /api/login/yo y PUT /api/login/yo/password",
                        "cancelacion", "DELETE /api/login/yo",
                        "oposicion", "PATCH /api/login/yo/oposicion"),
                "transferencias", "No se transfieren datos a terceros"
        );
    }
}
