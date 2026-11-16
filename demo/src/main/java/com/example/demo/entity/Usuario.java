package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    /** Hash PBKDF2 con sal; nunca se guarda ni se devuelve la contraseña en claro. */
    @Column(nullable = false, length = 255)
    private String passwordHash;

    /** Consentimiento: versión de la política aceptada y cuándo se aceptó. */
    @Column(nullable = false, length = 10)
    private String versionPolitica;

    @Column(nullable = false)
    private Instant fechaConsentimiento;

    @Column(nullable = false)
    private Instant fechaRegistro;

    /** Finalidad opcional. Desactivada por defecto; el derecho de Oposición la apaga. */
    @Column(nullable = false)
    private boolean aceptaComunicaciones;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String passwordHash, String versionPolitica) {
        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.versionPolitica = versionPolitica;
        this.fechaConsentimiento = Instant.now();
        this.fechaRegistro = this.fechaConsentimiento;
        this.aceptaComunicaciones = false;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getVersionPolitica() { return versionPolitica; }
    public Instant getFechaConsentimiento() { return fechaConsentimiento; }
    public Instant getFechaRegistro() { return fechaRegistro; }
    public boolean isAceptaComunicaciones() { return aceptaComunicaciones; }
    public void setAceptaComunicaciones(boolean aceptaComunicaciones) { this.aceptaComunicaciones = aceptaComunicaciones; }
}
