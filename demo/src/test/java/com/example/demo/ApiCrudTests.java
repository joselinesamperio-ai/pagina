package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalogo-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ApiCrudTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void limpiarBaseDeDatos() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM productos");
            statement.executeUpdate("DELETE FROM categorias");
        }
    }

    @Test
    void realizaCrudDeCategoriasYProductosRespetandoRelaciones() throws Exception {
        String categoria = mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Computación","descripcion":"Equipo y accesorios"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Computación"))
                .andReturn().getResponse().getContentAsString();
        Long categoriaId = idDe(categoria);

        mockMvc.perform(get("/api/categorias/{id}", categoriaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaId));
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Computación"));

        String producto = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop","precio":18500.00,"categoriaId":%d}
                                """.formatted(categoriaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.categoriaNombre").value("Computación"))
                .andReturn().getResponse().getContentAsString();
        Long productoId = idDe(producto);

        mockMvc.perform(get("/api/productos/{id}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));

        mockMvc.perform(put("/api/productos/{id}", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop Pro","precio":19999.99,"categoriaId":%d}
                                """.formatted(categoriaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Pro"));

        mockMvc.perform(delete("/api/categorias/{id}", categoriaId))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/productos/{id}", productoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/categorias/{id}", categoriaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Tecnología","descripcion":"Actualizada"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tecnología"));

        mockMvc.perform(delete("/api/categorias/{id}", categoriaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void rechazaReferenciasInvalidasDatosInvalidosYRecursosInexistentes() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop","precio":100,"categoriaId":999}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":" ","descripcion":"Inválida"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound());
    }

    private Long idDe(String json) {
        String id = json.replaceAll("(?s).*\"id\":(\\d+).*", "$1");
        return Long.valueOf(id);
    }
}
