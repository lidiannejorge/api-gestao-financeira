package com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.repository;

import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface SpringDataUsuarioRepository
        extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}