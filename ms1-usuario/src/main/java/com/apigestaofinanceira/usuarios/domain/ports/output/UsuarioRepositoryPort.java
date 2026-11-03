package com.apigestaofinanceira.usuarios.domain.ports.output;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    List<Usuario> listarTodos();

    void excluir(Long id);

    boolean existePorEmail(String email);
}
