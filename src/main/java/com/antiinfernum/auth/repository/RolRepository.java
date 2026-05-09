package com.antiinfernum.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antiinfernum.auth.model.Rol;

public interface RolRepository extends JpaRepository<Rol, String> {
    public Rol findByNombre(String nombre);
}
