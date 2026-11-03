package com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence;

import com.apigestaofinanceira.usuarios.domain.model.Usuario;
import com.apigestaofinanceira.usuarios.domain.ports.output.UsuarioRepositoryPort;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.mapper.UsuarioMapper;
import com.apigestaofinanceira.usuarios.infrastructure.adapters.output.persistence.repository.SpringDataUsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository repository;

    public UsuarioPersistenceAdapter(
            SpringDataUsuarioRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {

        var entity = UsuarioMapper.toEntity(usuario);

        var entitySalva = repository.save(entity);

        return UsuarioMapper.toDomain(entitySalva);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {

        return repository.findById(id)
                .map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {

        return repository.findByEmail(email)
                .map(UsuarioMapper::toDomain);
    }

    @Override
    public List<Usuario> listarTodos() {

        return repository.findAll()
                .stream()
                .map(UsuarioMapper::toDomain)
                .toList();
    }

    @Override
    public void excluir(Long id) {

        repository.deleteById(id);
    }

    @Override
    public boolean existePorEmail(String email) {

        return repository.existsByEmail(email);
    }
}