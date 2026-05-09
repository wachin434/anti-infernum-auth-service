package com.antiinfernum.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.antiinfernum.auth.model.Rol;
import com.antiinfernum.auth.repository.RolRepository;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
public class RolServiceTest {

    @Autowired
    private RolService rolService;

    @MockitoBean
    private RolRepository rolRepository;

    private static final String ROL_ID = "b1de73e0-e49a-4b1f-93ad-e4640e3b5db0";

    private Rol createRol() {
        return new Rol(ROL_ID, "admin");
    }

    @Test
    public void testFindAll() {
        when(rolRepository.findAll()).thenReturn(List.of(createRol()));

        List<Rol> roles = rolService.findAll();

        assertNotNull(roles);
        assertEquals(1, roles.size());
        verify(rolRepository, times(1)).findAll();
    }

    @Test
    public void testFindById() {
        Rol rol = createRol();
        when(rolRepository.findById(ROL_ID)).thenReturn(Optional.of(rol));

        Rol foundRol = rolService.findById(ROL_ID);

        assertNotNull(foundRol);
        assertEquals("admin", foundRol.getNombre());
    }

    @Test
    public void testFindByNombre() {
        Rol rol = createRol();
        when(rolRepository.findByNombre("admin")).thenReturn(
                Optional.of(rol));

        Rol foundRol = rolService.findByNombre("admin");

        assertNotNull(foundRol);
        assertEquals(ROL_ID, foundRol.getId());
    }

    @Test
    public void testSave() {
        Rol rol = createRol();
        when(rolRepository.save(rol)).thenReturn(rol);

        Rol savedRol = rolService.save(rol);

        assertNotNull(savedRol);
        assertEquals(ROL_ID, savedRol.getId());
    }

    @Test
    public void testDeleteById() {
        assertThrows(EntityNotFoundException.class, () -> {
            rolService.deleteById(ROL_ID);
        });
        when(rolRepository.findById(ROL_ID)).thenReturn(Optional.empty());
        Rol result = rolService.findById(ROL_ID);
        assertNull(result);
    }

    @Test
    public void testUpdate() {
        Rol rol = createRol();
        when(rolRepository.findById(ROL_ID)).thenReturn(Optional.of(rol));
        when(rolRepository.save(rol)).thenReturn(rol);

        assertThrows(EntityNotFoundException.class, () -> {
            Rol updatedRol = rolService.update(ROL_ID, rol);
            assertNotNull(updatedRol);
            assertEquals("admin", updatedRol.getNombre());
        });
    }

    @Test
    public void testPatch() {
        Rol rol = createRol();
        when(rolRepository.findById(ROL_ID)).thenReturn(Optional.of(rol));
        when(rolRepository.save(rol)).thenReturn(rol);

        Rol patchedRol = rolService.patch(ROL_ID, rol);
        assertNotNull(patchedRol);
        assertEquals("admin", patchedRol.getNombre());
    }
}
