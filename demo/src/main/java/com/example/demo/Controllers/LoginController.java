package com.example.demo.Controllers;

import com.example.demo.dto.CambioPasswordRequest;
import com.example.demo.dto.CancelacionRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RectificacionRequest;
import com.example.demo.dto.RegistroRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.ConflictoException;
import com.example.demo.exception.NoAutorizadoException;
import com.example.demo.exception.SolicitudInvalidaException;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.PasswordHasher;
import com.example.demo.service.PoliticaPrivacidad;
import com.example.demo.service.TokenService;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login con CRUD de la cuenta aplicando docs/POLITICA_PRIVACIDAD.md.
 *
 * Create  -> POST   /registro            (exige consentimiento)
 * Read    -> GET    /yo                  (derecho de ACCESO)
 * Update  -> PUT    /yo, /yo/password    (derecho de RECTIFICACIÓN)
 * Delete  -> DELETE /yo                  (derecho de CANCELACIÓN)
 *            PATCH  /yo/oposicion        (derecho de OPOSICIÓN)
 * No existe un endpoint que liste usuarios: cada titular solo ve sus propios datos.
 */
@RestController
@RequestMapping("/api/login")
public class LoginController {

    private static final String CREDENCIALES_INVALIDAS = "Credenciales inválidas";

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final String hashFicticio;

    public LoginController(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.hashFicticio = passwordHasher.hash("hash-ficticio");
    }

    // ---- Información (principio de información) ----

    @GetMapping("/politica-privacidad")
    public Map<String, Object> politica() {
        return PoliticaPrivacidad.resumen();
    }

    // ---- Create: registro con consentimiento ----

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        if (!Boolean.TRUE.equals(request.aceptaPoliticaPrivacidad())) {
            throw new SolicitudInvalidaException(
                    "Debes aceptar la política de privacidad (versión " + PoliticaPrivacidad.VERSION + ") para registrarte");
        }
        String correo = normaliza(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        Usuario usuario = new Usuario(request.nombre().trim(), correo,
                passwordHasher.hash(request.password()), PoliticaPrivacidad.VERSION);
        usuario.setAceptaComunicaciones(Boolean.TRUE.equals(request.aceptaComunicaciones()));
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    // ---- Login / logout ----

    @PostMapping
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(normaliza(request.correo())).orElse(null);
        // Se calcula un hash aunque el usuario no exista, para no revelar su existencia por tiempo de respuesta.
        String hashAComparar = usuario != null ? usuario.getPasswordHash() : hashFicticio;
        boolean valido = passwordHasher.verifica(request.password(), hashAComparar);
        if (usuario == null || !valido) {
            throw new NoAutorizadoException(CREDENCIALES_INVALIDAS);
        }
        return new LoginResponse(tokenService.crear(usuario.getId()), TokenService.DURACION.toSeconds(),
                UsuarioResponse.from(usuario));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(@RequestHeader(value = "Authorization", required = false) String authorization) {
        usuarioAutenticado(authorization);
        tokenService.revocar(extraeToken(authorization));
    }

    // ---- ARCO: Acceso ----

    @GetMapping("/yo")
    public UsuarioResponse acceso(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return UsuarioResponse.from(usuarioAutenticado(authorization));
    }

    // ---- ARCO: Rectificación ----

    @PutMapping("/yo")
    public UsuarioResponse rectificar(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RectificacionRequest request) {
        Usuario usuario = usuarioAutenticado(authorization);
        String correo = normaliza(request.correo());
        if (!correo.equals(usuario.getCorreo()) && usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @PutMapping("/yo/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CambioPasswordRequest request) {
        Usuario usuario = usuarioAutenticado(authorization);
        if (!passwordHasher.verifica(request.passwordActual(), usuario.getPasswordHash())) {
            throw new NoAutorizadoException(CREDENCIALES_INVALIDAS);
        }
        usuario.setPasswordHash(passwordHasher.hash(request.passwordNueva()));
        usuarioRepository.save(usuario);
        tokenService.revocarTodas(usuario.getId());
    }

    // ---- ARCO: Oposición ----

    @PatchMapping("/yo/oposicion")
    public UsuarioResponse oponerse(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Usuario usuario = usuarioAutenticado(authorization);
        usuario.setAceptaComunicaciones(false);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    // ---- ARCO: Cancelación ----

    @DeleteMapping("/yo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CancelacionRequest request) {
        Usuario usuario = usuarioAutenticado(authorization);
        if (!passwordHasher.verifica(request.password(), usuario.getPasswordHash())) {
            throw new NoAutorizadoException(CREDENCIALES_INVALIDAS);
        }
        tokenService.revocarTodas(usuario.getId());
        usuarioRepository.delete(usuario);
    }

    // ---- Acreditación de la personalidad ----

    private Usuario usuarioAutenticado(String authorization) {
        String token = extraeToken(authorization);
        Long id = tokenService.usuarioDe(token)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida o caducada"));
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NoAutorizadoException("Sesión inválida o caducada"));
    }

    private String extraeToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new NoAutorizadoException("Falta el encabezado Authorization: Bearer <token>");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private String normaliza(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
