package com.api_gestao_financeira.service;

import com.api_gestao_financeira.dto.UsuarioRequestDTO;
import com.api_gestao_financeira.dto.UsuarioResponseDTO;
import com.api_gestao_financeira.exceptions.EmailJaCadastradoException;
import com.api_gestao_financeira.exceptions.UsuarioNaoEncontradoException;
import com.api_gestao_financeira.model.Usuario;
import com.api_gestao_financeira.repository.UsuarioRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {

        if (repository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setTelefone(dto.getTelefone());

        Usuario saved = repository.save(usuario);

        return toResponse(saved);
    }

    public List<UsuarioResponseDTO> findAll() {

        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO findById(Long id) {

        Usuario user = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException("Usuário não encontrado"));

        return toResponse(user);
    }

    public UsuarioResponseDTO update(Long id, UsuarioRequestDTO dto) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException("Usuário não encontrado"));

        repository.findByEmail(dto.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new EmailJaCadastradoException("Email já cadastrado");
                });

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());

        Usuario updated = repository.save(usuario);

        return toResponse(updated);
    }

    public void delete(Long id) {

        Usuario usuario = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException("Usuário não encontrado"));

        repository.delete(usuario);
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }
}
