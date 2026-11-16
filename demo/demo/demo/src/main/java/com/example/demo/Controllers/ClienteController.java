package com.example.demo.Controllers;

import com.example.demo.dto.ClienteRequest;
import com.example.demo.dto.ClienteResponse;
import com.example.demo.entity.Cliente;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.service.TokenService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
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

/** CRUD de clientes. Contiene datos personales: todas las rutas exigen sesión iniciada. */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private static final String AUTH = "Authorization";

    private final ClienteRepository clienteRepository;
    private final TokenService tokenService;

    public ClienteController(ClienteRepository clienteRepository, TokenService tokenService) {
        this.clienteRepository = clienteRepository;
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestHeader(value = AUTH, required = false) String authorization) {
        tokenService.exigirSesion(authorization);
        return clienteRepository.findAll().stream().map(ClienteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@RequestHeader(value = AUTH, required = false) String authorization,
            @PathVariable Long id) {
        tokenService.exigirSesion(authorization);
        return ClienteResponse.from(obtener(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@RequestHeader(value = AUTH, required = false) String authorization,
            @Valid @RequestBody ClienteRequest r) {
        tokenService.exigirSesion(authorization);
        return ClienteResponse.from(clienteRepository.save(
                new Cliente(r.nombre().trim(), normaliza(r.correo()), vacioANulo(r.telefono()))));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@RequestHeader(value = AUTH, required = false) String authorization,
            @PathVariable Long id, @Valid @RequestBody ClienteRequest r) {
        tokenService.exigirSesion(authorization);
        Cliente c = obtener(id);
        c.setNombre(r.nombre().trim());
        c.setCorreo(normaliza(r.correo()));
        c.setTelefono(vacioANulo(r.telefono()));
        return ClienteResponse.from(clienteRepository.save(c));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(value = AUTH, required = false) String authorization,
            @PathVariable Long id) {
        tokenService.exigirSesion(authorization);
        clienteRepository.delete(obtener(id));
    }

    private Cliente obtener(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente", id));
    }

    private static String normaliza(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static String vacioANulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
