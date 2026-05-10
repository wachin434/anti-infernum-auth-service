package com.antiinfernum.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.antiinfernum.auth.model.Usuario;
import com.antiinfernum.auth.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
public class UsuarioServicePrueba {

    @Autowired
    private UsuarioService usuarioService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private static final String USUARIO_ID = "d3b2b2f0-1234-4f1a-9876-abcdef123456";
    private static final String USUARIO_EMAIL = "test@example.com";
    private static final String USUARIO_CONTRA = "contrasena123";

    private Usuario createUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setNombre("Usuario de prueba");
        usuario.setEmail(USUARIO_EMAIL);
        usuario.setContra(new BCryptPasswordEncoder().encode(USUARIO_CONTRA));
        usuario.setFechaRegistro(new Date(System.currentTimeMillis()));
        return usuario;
    }

    @Test
    public void pruebaBuscarTodos() {
        Usuario usuario = createUsuario();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> usuarios = usuarioService.findAll();

        assertNotNull(usuarios);
        assertEquals(1, usuarios.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void pruebaBuscarPorId() {
        Usuario usuario = createUsuario();
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        Usuario foundUsuario = usuarioService.findById(USUARIO_ID);

        assertNotNull(foundUsuario);
        assertEquals(USUARIO_EMAIL, foundUsuario.getEmail());
    }

    @Test
    public void pruebaBuscarPorEmail() {
        Usuario usuario = createUsuario();
        when(usuarioRepository.findByEmail(USUARIO_EMAIL)).thenReturn(Optional.of(usuario));

        Usuario foundUsuario = usuarioService.findByEmail(USUARIO_EMAIL);

        assertNotNull(foundUsuario);
        assertEquals(USUARIO_ID, foundUsuario.getId());
    }

    @Test
    public void pruebaGuardarCodificaContrasena() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario de prueba");
        usuario.setEmail(USUARIO_EMAIL);
        usuario.setContra(USUARIO_CONTRA);
        usuario.setFechaRegistro(new Date(System.currentTimeMillis()));

        when(usuarioRepository.findByEmail(USUARIO_EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario savedUsuario = usuarioService.save(usuario);

        assertNotNull(savedUsuario);
        assertEquals(USUARIO_EMAIL, savedUsuario.getEmail());
        assertNotEquals(USUARIO_CONTRA, savedUsuario.getContra());
        assertTrue(new BCryptPasswordEncoder().matches(USUARIO_CONTRA, savedUsuario.getContra()));
    }

    @Test
    public void pruebaLoginExitoso() {
        Usuario usuario = createUsuario();
        when(usuarioRepository.findByEmail(USUARIO_EMAIL)).thenReturn(Optional.of(usuario));

        Usuario loggedUsuario = usuarioService.login(USUARIO_EMAIL, USUARIO_CONTRA);

        assertNotNull(loggedUsuario);
        assertEquals(USUARIO_EMAIL, loggedUsuario.getEmail());
    }

    @Test
    public void pruebaLoginContrasenaInvalida() {
        Usuario usuario = createUsuario();
        when(usuarioRepository.findByEmail(USUARIO_EMAIL)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.login(USUARIO_EMAIL, "wrongpass"));
    }

    @Test
    public void pruebaEliminarPorIdNoEncontrado() {
        when(usuarioRepository.existsById(USUARIO_ID)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> usuarioService.deleteById(USUARIO_ID));
    }

    @Test
    public void pruebaActualizar() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario actualizado");
        usuario.setEmail(USUARIO_EMAIL);
        usuario.setContra(USUARIO_CONTRA);
        usuario.setFechaRegistro(new Date(System.currentTimeMillis()));

        when(usuarioRepository.existsById(USUARIO_ID)).thenReturn(true);
        when(usuarioRepository.findByEmail(USUARIO_EMAIL))
                .thenReturn(Optional.of(new Usuario(USUARIO_ID, "Usuario actualizado", USUARIO_EMAIL,
                        new BCryptPasswordEncoder().encode(USUARIO_CONTRA), new Date(System.currentTimeMillis()))));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario updatedUsuario = usuarioService.update(USUARIO_ID, usuario);

        assertNotNull(updatedUsuario);
        assertEquals(USUARIO_ID, updatedUsuario.getId());
        assertTrue(new BCryptPasswordEncoder().matches(USUARIO_CONTRA, updatedUsuario.getContra()));
    }

    @Test
    public void pruebaPatch() {
        Usuario usuarioExistente = createUsuario();
        Usuario patchData = new Usuario();
        patchData.setNombre("Usuario parcheado");
        patchData.setContra("nuevacontrasena");

        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.findByEmail(USUARIO_EMAIL)).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario patchedUsuario = usuarioService.patch(USUARIO_ID, patchData);

        assertNotNull(patchedUsuario);
        assertEquals("Usuario parcheado", patchedUsuario.getNombre());
        assertTrue(new BCryptPasswordEncoder().matches("nuevacontrasena", patchedUsuario.getContra()));
    }
}
