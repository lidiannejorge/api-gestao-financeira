package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListTransacoesUseCase {

    private final TransacaoRepositoryPort repositoryPort;

    public ListTransacoesUseCase(TransacaoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public List<Transacao> executar(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fim,
            TipoTransacao tipo) {

        // Valida se a data final não é anterior à data inicial
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "A data final não pode ser anterior à data inicial."
            );
        }

        return repositoryPort.buscarComFiltros(
                usuarioId, inicio, fim, tipo
        );
    }
}