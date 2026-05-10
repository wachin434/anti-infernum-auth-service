package com.antiinfernum.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.antiinfernum.auth.model.Usuario;
import com.antiinfernum.auth.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public Usuario save(Usuario usuario) {
        if (!validarUsuario(usuario)) {
            throw new IllegalArgumentException(
                    "El usuario es invalido: El nombre, email y contraseña no pueden ser nulos o estar vacios.");
        }
        if (validarExistenciaPorEmail(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "El usuario es invalido: Ya existe un usuario con el email " + usuario.getEmail() + ".");
        }
        usuario.setContra(encodePassword(usuario.getContra()));
        return usuarioRepository.save(usuario);
    }

    public void deleteById(String id) {
        if (!validarExistencia(id)) {
            throw new EntityNotFoundException("El usuario con ID " + id + " no existe.");
        }
        usuarioRepository.deleteById(id);
    }

    public Usuario update(String id, Usuario usuario) {
        if (!validarExistencia(id)) {
            throw new EntityNotFoundException("El usuario con ID " + id + " no existe.");
        }
        if (!validarUsuario(usuario)) {
            throw new IllegalArgumentException(
                    "El usuario es invalido: El nombre, email y contraseña no pueden ser nulos o estar vacios.");
        }
        if (validarExistenciaPorEmail(usuario.getEmail())) {
            Usuario usuarioPorEmail = usuarioRepository.findByEmail(usuario.getEmail()).orElse(null);
            if (usuarioPorEmail != null && !usuarioPorEmail.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "El usuario es invalido: Ya existe un usuario con el email " + usuario.getEmail() + ".");
            }
        }
        usuario.setId(id);
        usuario.setContra(encodePassword(usuario.getContra()));
        return usuarioRepository.save(usuario);
    }

    public Usuario patch(String id, Usuario usuario) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
                        usuarioExistente.setNombre(usuario.getNombre());
                    }
                    if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                        if (validarExistenciaPorEmail(usuario.getEmail())) {
                            Usuario usuarioPorEmail = usuarioRepository.findByEmail(usuario.getEmail()).orElse(null);
                            if (usuarioPorEmail != null && !usuarioPorEmail.getId().equals(id)) {
                                throw new IllegalArgumentException(
                                        "El usuario es invalido: Ya existe un usuario con el email "
                                                + usuario.getEmail() + ".");
                            }
                        }
                        usuarioExistente.setEmail(usuario.getEmail());
                    }
                    if (usuario.getContra() != null && !usuario.getContra().trim().isEmpty()) {
                        usuarioExistente.setContra(encodePassword(usuario.getContra()));
                    }
                    if (usuario.getFechaRegistro() != null) {
                        usuarioExistente.setFechaRegistro(usuario.getFechaRegistro());
                    }
                    return usuarioRepository.save(usuarioExistente);
                })
                .orElseThrow(() -> new EntityNotFoundException("El usuario con ID " + id + " no existe."));
    }

    public Usuario login(String email, String contra) {
        if (email == null || email.trim().isEmpty() || contra == null || contra.trim().isEmpty()) {
            throw new IllegalArgumentException("El email y la contraseña son requeridos para iniciar sesión.");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("El usuario con email " + email + " no existe."));
        if (!passwordEncoder.matches(contra, usuario.getContra())) {
            throw new IllegalArgumentException("Credenciales invalidas: Email o contraseña incorrectos.");
        }
        return usuario;
    }

    private String encodePassword(String contra) {
        return passwordEncoder.encode(contra);
    }

    private boolean validarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return false;
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return false;
        }
        if (usuario.getContra() == null || usuario.getContra().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean validarExistenciaPorEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    private boolean validarExistencia(String id) {
        return usuarioRepository.existsById(id);
    }
}
