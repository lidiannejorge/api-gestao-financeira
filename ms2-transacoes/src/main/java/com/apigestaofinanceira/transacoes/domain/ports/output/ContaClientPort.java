package com.apigestaofinanceira.transacoes.domain.ports.output;

import java.math.BigDecimal;

public interface ContaClientPort {

    ContaDados consultarConta(Long usuarioId);

    void atualizarSaldo(Long usuarioId, BigDecimal novoSaldo);

    record ContaDados(
            String id,
            Long usuarioId,
            BigDecimal saldo,
            BigDecimal limiteDiario,
            BigDecimal limitePorTransacao
    ) {
    }
}