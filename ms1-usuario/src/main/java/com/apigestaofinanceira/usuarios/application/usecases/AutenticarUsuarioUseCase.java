package com.apigestaofinanceira.usuarios.application.usecases;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;

import java.util.Optional;

public interface AutenticarUsuarioUseCase {

    /**
     * Valida email e senha. Retorna o usuário autenticado (sem a senha
     * em texto puro) ou Optional.empty() se as credenciais forem
     * inválidas.
     */
    Optional<Usuario> autenticar(String email, String password);
}