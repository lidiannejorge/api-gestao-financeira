package com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.mapper;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.CreateUsuarioRequestDTO;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.input.web.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UsuarioWebMapper {
    public Usuario toDomain(CreateUsuarioRequestDTO dto) {
        return Usuario.create(
                dto.getName(),
                dto.getEmail(),
                dto.getPassword()
        );
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getCreatedAt()
        );
    }
}
