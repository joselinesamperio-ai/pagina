package com.example.demo.Controllers;

import com.example.demo.dto.ProveedorRequest;
import com.example.demo.dto.ProveedorResponse;
import com.example.demo.entity.Proveedor;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.ProveedorRepository;
import com.example.demo.service.TokenService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    private final TokenService tokenService;

    public ProveedorController(ProveedorRepository proveedorRepository, TokenService tokenService) {
        this.proveedorRepository = proveedorRepository;
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<ProveedorResponse> listar(@RequestHeader(value = "Authorization", required = false) String authorization) {
        tokenService.exigirSesion(authorization);
        return proveedorRepository.findAll().stream().map(ProveedorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProveedorResponse buscar(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
        tokenService.exigirSesion(authorization);
        return ProveedorResponse.from(obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorResponse crear(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody ProveedorRequest r) {
        tokenService.exigirSesion(authorization);
        return ProveedorResponse.from(proveedorRepository.save(
                new Proveedor(r.nombre(), vacioANulo(r.correo()), vacioANulo(r.telefono()), vacioANulo(r.direccion()))));
    }

    @PutMapping("/{id}")
    public ProveedorResponse actualizar(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest r) {
        tokenService.exigirSesion(authorization);
        Proveedor p = obtener(id);
        p.setNombre(r.nombre());
        p.setCorreo(vacioANulo(r.correo()));
        p.setTelefono(vacioANulo(r.telefono()));
        p.setDireccion(vacioANulo(r.direccion()));
        return ProveedorResponse.from(proveedorRepository.save(p));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
        tokenService.exigirSesion(authorization);
        proveedorRepository.delete(obtener(id));
    }

    private Proveedor obtener(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proveedor", id));
    }

    private static String vacioANulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
