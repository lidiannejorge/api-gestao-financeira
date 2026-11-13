package com.apigestaofinanceira.transacoes.domain.ports.output;

import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepositoryPort {

    Transacao salvar(Transacao transacao);

    Optional<Transacao> buscarPorId(Long id);

    List<Transacao> buscarPorUsuarioId(Long usuarioId);

    List<Transacao> buscarComFiltros(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoTransacao tipo
    );
}