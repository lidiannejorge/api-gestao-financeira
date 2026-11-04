package com.apigestaofinanceira.usuarios.application.usecases;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;

import java.util.Optional;

public interface AutenticarUsuarioUseCase {

    Optional<Usuario> autenticar(String email, String password);
}
