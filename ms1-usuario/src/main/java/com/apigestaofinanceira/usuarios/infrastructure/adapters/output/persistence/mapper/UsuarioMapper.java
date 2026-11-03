package com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.mapper;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.entity.UsuarioEntity;

public class UsuarioMapper {

    public static UsuarioEntity toEntity(Usuario usuario) {

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setName(usuario.getName());
        entity.setEmail(usuario.getEmail());
        entity.setPassword(usuario.getPassword());
        entity.setCreatedAt(usuario.getCreatedAt());
        return entity;
    }
    public static Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getCreatedAt()
        );
    }
}