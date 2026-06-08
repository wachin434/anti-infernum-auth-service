package com.antiinfernum.auth.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.antiinfernum.auth.dto.AuthResponse;
import com.antiinfernum.auth.model.Rol;
import com.antiinfernum.auth.model.Usuario;
import com.antiinfernum.auth.repository.RolRepository;
import com.antiinfernum.auth.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

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

        // Establecer rol predeterminado
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            Optional<Rol> rol = rolRepository.findByNombre("usuario");
            if (rol.isPresent()) {
                usuario.setRoles(Set.of(rol.get()));
            } else {
                throw new IllegalStateException("No se encontró el rol predeterminado 'usuario'.");
            }
        }
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
                        if (!validarEmail(usuario.getEmail())) {
                            throw new IllegalArgumentException("El email no tiene un formato valido.");
                        }
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
                    if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                        usuarioExistente.setRoles(usuario.getRoles());
                    }
                    return usuarioRepository.save(usuarioExistente);
                })
                .orElseThrow(() -> new EntityNotFoundException("El usuario con ID " + id + " no existe."));
    }

    public AuthResponse login(String email, String contra) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(contra, usuario.getContra())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String token = jwtService.generateToken(usuario);

        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        return new AuthResponse(token, usuario.getEmail(), roles);
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
        String email = usuario.getEmail();
        if (!validarEmail(email)) {
            return false;
        }
        if (usuario.getContra() == null || usuario.getContra().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean validarEmail(String email) {
        return email != null && !email.trim().isEmpty()
                && email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean validarExistenciaPorEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    private boolean validarExistencia(String id) {
        return usuarioRepository.existsById(id);
    }
}
