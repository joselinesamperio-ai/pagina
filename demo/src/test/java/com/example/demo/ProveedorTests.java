package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:proveedores-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ProveedorTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void realizaCrudDeProveedores() throws Exception {
        String creado = mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Distribuidora Norte\",\"correo\":\"ventas@norte.com\",\"telefono\":\"5512345678\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Distribuidora Norte"))
                .andReturn().getResponse().getContentAsString();
        String id = creado.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/proveedores/{id}", id)).andExpect(status().isOk());
        mockMvc.perform(get("/api/proveedores")).andExpect(status().isOk());

        mockMvc.perform(put("/api/proveedores/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Distribuidora Sur\",\"correo\":\"\",\"telefono\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Distribuidora Sur"))
                .andExpect(jsonPath("$.correo").doesNotExist());

        mockMvc.perform(delete("/api/proveedores/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/proveedores/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void rechazaDatosInvalidosYDuplicados() throws Exception {
        mockMvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"X\",\"correo\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Dup\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Dup\"}"))
                .andExpect(status().isConflict());
    }
}
