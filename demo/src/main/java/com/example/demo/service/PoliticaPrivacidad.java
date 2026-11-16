package com.example.demo.service;

import java.util.List;
import java.util.Map;

/** Datos vigentes de la política de privacidad (ver docs/POLITICA_PRIVACIDAD.md). */
public final class PoliticaPrivacidad {

    public static final String VERSION = "1.2";

    private PoliticaPrivacidad() {
    }

    public static Map<String, Object> resumen() {
        return Map.of(
                "version", VERSION,
                "documento", "docs/POLITICA_PRIVACIDAD.md",
                "datosRecabados", List.of("nombre", "correo", "contraseña (solo hash)", "consentimiento",
                        "clientes: nombre, correo y teléfono", "proveedores: nombre, correo, teléfono y dirección"),
                "finalidadesNecesarias", List.of("administrar la cuenta", "autenticar", "avisos de seguridad por correo",
                        "registro de clientes y proveedores", "atender derechos ARCO"),
                "finalidadesOpcionales", List.of("comunicaciones del proyecto (desactivadas por defecto)"),
                "derechosArco", Map.of(
                        "acceso", "GET /api/login/yo",
                        "rectificacion", "PUT /api/login/yo y PUT /api/login/yo/password",
                        "cancelacion", "DELETE /api/login/yo",
                        "oposicion", "PATCH /api/login/yo/oposicion"),
                "transferencias", "Solo correo y nombre al proveedor de envío de correo (Brevo), si está activado, para los avisos de seguridad"
        );
    }
}
