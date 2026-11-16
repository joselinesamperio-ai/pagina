package com.example.demo.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Avisos de seguridad por correo (cuenta creada e inicio de sesión) mediante la API HTTPS de Brevo.
 * Queda DESACTIVADO si no se configuran BREVO_API_KEY y MAIL_FROM. Se envía en segundo plano
 * y un fallo nunca afecta al registro ni al login. No se registran correos ni nombres en los logs.
 */
@Component
public class NotificadorCorreo {

    private static final Logger LOG = LoggerFactory.getLogger(NotificadorCorreo.class);
    private static final String URL = "https://api.brevo.com/v3/smtp/email";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final String apiKey;
    private final String remitente;
    private final String nombreRemitente;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public NotificadorCorreo(@Value("${notificaciones.brevo.api-key:}") String apiKey,
            @Value("${notificaciones.correo.remitente:}") String remitente,
            @Value("${notificaciones.correo.nombre-remitente:Catálogo}") String nombreRemitente) {
        this.apiKey = apiKey.trim();
        this.remitente = remitente.trim();
        this.nombreRemitente = nombreRemitente;
    }

    public boolean activo() {
        return !apiKey.isEmpty() && !remitente.isEmpty();
    }

    public void cuentaCreada(String nombre, String correo) {
        enviar(nombre, correo, "Tu cuenta en Catálogo fue creada",
                "Hola " + nombre + ",\n\nTu cuenta en Catálogo se creó el " + ahora()
                + " (hora de Ciudad de México) con este correo.\n\n"
                + "Aceptaste la política de privacidad vigente. Puedes consultar, corregir o eliminar tus datos "
                + "en la sección \"Mi cuenta\".\n\nSi no fuiste tú, ignora este mensaje y responde al "
                + "contacto de privacidad para solicitar la cancelación de la cuenta.");
    }

    public void inicioDeSesion(String nombre, String correo) {
        enviar(nombre, correo, "Nuevo inicio de sesión en Catálogo",
                "Hola " + nombre + ",\n\nSe inició sesión en tu cuenta el " + ahora()
                + " (hora de Ciudad de México).\n\nSi fuiste tú, no necesitas hacer nada. Si no reconoces este "
                + "acceso, cambia tu contraseña en \"Mi cuenta\" (esto cierra todas las sesiones abiertas).");
    }

    private static String ahora() {
        return ZonedDateTime.now(ZONA).format(FORMATO);
    }

    private void enviar(String nombre, String correo, String asunto, String texto) {
        if (!activo()) {
            return;
        }
        HttpRequest peticion = peticionBrevo(nombre, correo, asunto, texto);
        cliente.sendAsync(peticion, HttpResponse.BodyHandlers.discarding())
                .thenAccept(r -> {
                    if (r.statusCode() >= 300) {
                        LOG.warn("El proveedor de correo respondió con estado {}", r.statusCode());
                    }
                })
                .exceptionally(e -> {
                    LOG.warn("No se pudo enviar el aviso por correo ({})", e.getClass().getSimpleName());
                    return null;
                });
    }

    private HttpRequest peticionBrevo(String nombre, String correo, String asunto, String texto) {
        String cuerpo = "{\"sender\":{\"name\":" + json(nombreRemitente) + ",\"email\":" + json(remitente) + "},"
                + "\"to\":[{\"email\":" + json(correo) + ",\"name\":" + json(nombre) + "}],"
                + "\"subject\":" + json(asunto) + ",\"textContent\":" + json(texto) + "}";
        return HttpRequest.newBuilder(URI.create(URL))
                .timeout(Duration.ofSeconds(10))
                .header("api-key", apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
    }

    /** Escapa una cadena como literal JSON. */
    static String json(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
