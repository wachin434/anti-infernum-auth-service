package com.antiinfernum.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antiinfernum.auth.model.Rol;

public interface RolRepository extends JpaRepository<Rol, String> {
    public Optional<Rol> findByNombre(String nombre);
}
