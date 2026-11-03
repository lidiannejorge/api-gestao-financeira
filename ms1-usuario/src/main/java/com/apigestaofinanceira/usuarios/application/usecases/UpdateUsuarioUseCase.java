package com.apigestaofinanceira.usuarios.application.usecases;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;

public interface UpdateUsuarioUseCase {


        Usuario atualizar(Long id, Usuario usuario);
    }

