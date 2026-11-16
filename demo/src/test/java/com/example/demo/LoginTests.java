package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:login-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class LoginTests {

    private static final String REGISTRO = """
            {"nombre":"Ana Pérez","correo":"Ana@Correo.com","password":"secreto123","aceptaPoliticaPrivacidad":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpiar() {
        usuarioRepository.deleteAll();
    }

    @Test
    void registroExigeAceptarLaPolitica() throws Exception {
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"a@a.com","password":"secreto123","aceptaPoliticaPrivacidad":false}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","correo":"a@a.com","password":"secreto123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registroGuardaConsentimientoYNuncaDevuelveLaContrasena() throws Exception {
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("ana@correo.com"))
                .andExpect(jsonPath("$.versionPolitica").value("1.0"))
                .andExpect(jsonPath("$.aceptaComunicaciones").value(false))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO))
                .andExpect(status().isConflict());
        org.junit.jupiter.api.Assertions.assertFalse(
                usuarioRepository.findAll().get(0).getPasswordHash().contains("secreto123"));
    }

    @Test
    void loginConCredencialesInvalidasDaMensajeGenerico() throws Exception {
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO));
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@correo.com\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"nadie@correo.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    void ejerceLosCuatroDerechosArco() throws Exception {
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"Ana Pérez","correo":"ana@correo.com","password":"secreto123",
                         "aceptaPoliticaPrivacidad":true,"aceptaComunicaciones":true}
                        """)).andExpect(status().isCreated());
        String respuesta = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ANA@correo.com\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        String auth = "Bearer " + token;

        // Sin token no se accede a nada
        mockMvc.perform(get("/api/login/yo")).andExpect(status().isUnauthorized());

        // Acceso
        mockMvc.perform(get("/api/login/yo").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"))
                .andExpect(jsonPath("$.aceptaComunicaciones").value(true));

        // Rectificación
        mockMvc.perform(put("/api/login/yo").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana María Pérez\",\"correo\":\"ana.maria@correo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana María Pérez"))
                .andExpect(jsonPath("$.correo").value("ana.maria@correo.com"));

        // Oposición
        mockMvc.perform(patch("/api/login/yo/oposicion").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceptaComunicaciones").value(false));

        // Cancelación: exige contraseña correcta
        mockMvc.perform(delete("/api/login/yo").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/login/yo").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secreto123\"}"))
                .andExpect(status().isNoContent());

        // La cuenta y las sesiones desaparecen
        mockMvc.perform(get("/api/login/yo").header("Authorization", auth)).andExpect(status().isUnauthorized());
        org.junit.jupiter.api.Assertions.assertEquals(0, usuarioRepository.count());
    }

    @Test
    void cambiarPasswordCierraSesiones() throws Exception {
        mockMvc.perform(post("/api/login/registro").contentType(MediaType.APPLICATION_JSON).content(REGISTRO));
        String respuesta = mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@correo.com\",\"password\":\"secreto123\"}"))
                .andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + respuesta.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(put("/api/login/yo/password").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"secreto123\",\"passwordNueva\":\"otraClave456\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/login/yo").header("Authorization", auth)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@correo.com\",\"password\":\"otraClave456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void publicaLaPoliticaDePrivacidad() throws Exception {
        mockMvc.perform(get("/api/login/politica-privacidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"));
    }
}
