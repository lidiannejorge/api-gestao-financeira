package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence;

import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.entity.TransacaoEntity;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.mapper.TransacaoMapper;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.output.persistence.repository.SpringDataTransacaoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class TransacaoPersistenceAdapter implements TransacaoRepositoryPort {

    private final SpringDataTransacaoRepository repository;
    private final TransacaoMapper mapper;

    public TransacaoPersistenceAdapter(
            SpringDataTransacaoRepository repository,
            TransacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoEntity entity = mapper.toEntity(transacao);
        TransacaoEntity salva = repository.save(entity);

        return mapper.toDomain(salva);
    }

    @Override
    public Optional<Transacao> buscarPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Transacao> buscarPorUsuarioId(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Transacao> buscarComFiltros(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoTransacao tipo) {

        return repository.buscarComFiltros(usuarioId, inicio, fim, tipo)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}