package com.apigestaofinanceira.usuarios.application.services;


import com.apigestaofinanceira.usuarios.application.usecases.*;
import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.domain.ports.output.UsuarioRepositoryPort;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.EmailJaCadastradoException;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.UsuarioNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements
        CreateUsuarioUseCase,
        FindUsuarioUseCase,
        UpdateUsuarioUseCase,
        DeleteUsuarioUseCase,
        AutenticarUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final BCryptPasswordEncoder passwordEncoder;
    public UsuarioService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public Usuario executar(Usuario usuario) {
        if (usuarioRepositoryPort.existePorEmail(usuario.getEmail())) {
            throw new EmailJaCadastradoException(usuario.getEmail());
        }
        String senhaCriptografada =
                passwordEncoder.encode(usuario.getPassword());
        Usuario novoUsuario = Usuario.create(
                usuario.getName(),
                usuario.getEmail(),
                senhaCriptografada
        );
        return usuarioRepositoryPort.salvar(novoUsuario);
    }
    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepositoryPort.buscarPorId(id);
    }
    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepositoryPort.listarTodos();
    }
    @Override
    public Usuario atualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepositoryPort
                .buscarPorId(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        Optional<Usuario> usuarioComEmail =
                usuarioRepositoryPort.buscarPorEmail(usuario.getEmail());
        if (usuarioComEmail.isPresent()
                && !usuarioComEmail.get().getId().equals(id)) {
            throw new EmailJaCadastradoException(usuario.getEmail());
        }
        usuarioExistente.update(
                usuario.getName(),
                usuario.getEmail()
        );
        return usuarioRepositoryPort.salvar(usuarioExistente);
    }
    @Override
    public void excluir(Long id) {
        usuarioRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        usuarioRepositoryPort.excluir(id);
    }
    @Override
    public Optional<Usuario> autenticar(String email, String password) {
        return usuarioRepositoryPort.buscarPorEmail(email)
                .filter(usuario ->
                        passwordEncoder.matches(password, usuario.getPassword()));
    }
}