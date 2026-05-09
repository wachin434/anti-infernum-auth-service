package com.antiinfernum.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.antiinfernum.auth.model.Rol;
import com.antiinfernum.auth.repository.RolRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    public Rol findById(String id) {
        return rolRepository.findById(id).orElse(null);
    }

    public Rol findByNombre(String nombre) {
        return rolRepository.findByNombre(nombre).orElse(null);
    }

    public Rol save(Rol rol) {
        if (!validarRol(rol)) {
            throw new IllegalArgumentException(
                    "El rol es invalido: El nombre del rol no puede ser nulo o estar vacio.");
        }
        if (validarExistenciaPorNombre(rol.getNombre())) {
            throw new IllegalArgumentException(
                    "El rol es invalido: Ya existe un rol con el nombre " + rol.getNombre() + ".");
        }
        return rolRepository.save(rol);
    }

    public void deleteById(String id) {
        if (!validarExistencia(id)) {
            throw new EntityNotFoundException("El rol con ID " + id + " no existe.");
        }
        rolRepository.deleteById(id);
    }

    public Rol update(String id, Rol rol) {
        if (!validarExistencia(id)) {
            throw new EntityNotFoundException("El rol con ID " + id + " no existe.");
        }
        if (!validarRol(rol)) {
            throw new IllegalArgumentException(
                    "El rol es invalido: El nombre del rol no puede ser nulo o estar vacio.");
        }
        rol.setId(id);
        return rolRepository.save(rol);
    }

    public Rol patch(String id, Rol rol) {
        return rolRepository.findById(id)
                .map(rolExistente -> {
                    if (rol.getNombre() != null && !rol.getNombre().trim().isEmpty()) {
                        rolExistente.setNombre(rol.getNombre());
                    }
                    return rolRepository.save(rolExistente);
                })
                .orElseThrow(() -> new EntityNotFoundException("El rol con ID " + id + " no existe."));
    }

    private boolean validarRol(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean validarExistenciaPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre).isPresent();
    }

    private boolean validarExistencia(String id) {
        return rolRepository.existsById(id);
    }
}
