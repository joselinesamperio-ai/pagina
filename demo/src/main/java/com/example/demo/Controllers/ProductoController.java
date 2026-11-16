package com.example.demo.Controllers;

import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.entity.Categoria;
import com.example.demo.entity.Producto;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Transactional
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse buscar(@PathVariable Long id) {
        return ProductoResponse.from(obtenerProducto(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        Categoria categoria = obtenerCategoria(request.categoriaId());
        return ProductoResponse.from(productoRepository.save(
                new Producto(request.nombre(), request.precio(), categoria)
        ));
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        Producto producto = obtenerProducto(id);
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        producto.setCategoria(obtenerCategoria(request.categoriaId()));
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoRepository.delete(obtenerProducto(id));
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto", id));
    }

    private Categoria obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría", id));
    }
}
