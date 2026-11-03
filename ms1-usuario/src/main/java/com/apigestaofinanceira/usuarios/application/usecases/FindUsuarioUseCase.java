package com.apigestaofinanceira.usuarios.application.usecases;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
public interface FindUsuarioUseCase {

    Optional<Usuario> buscarPorId(Long id);
    List<Usuario> listarTodos();
}