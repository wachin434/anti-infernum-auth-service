package com.antiinfernum.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antiinfernum.auth.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

}
