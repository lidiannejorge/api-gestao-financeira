package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.mockapi;

import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockApiContaClient implements ContaClientPort {

    private final RestClient mockApiRestClient;

    public MockApiContaClient(
            @Qualifier("mockApiRestClient") RestClient mockApiRestClient) {
        this.mockApiRestClient = mockApiRestClient;
    }

    @Override
    public ContaDados consultarConta(Long usuarioId) {

        List<ContaResponse> contas = mockApiRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/contas")
                        .queryParam("usuarioId", usuarioId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (contas == null || contas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Conta não encontrada para o usuário informado."
            );
        }

        ContaResponse conta = contas.get(0);

        return converterConta(conta);
    }

    @Override
    public void atualizarSaldo(Long usuarioId, BigDecimal novoSaldo) {

        ContaDados conta = consultarConta(usuarioId);

        ContaAtualizacao atualizacao = new ContaAtualizacao(
                usuarioId,
                novoSaldo,
                conta.limiteDiario(),
                conta.limitePorTransacao()
        );

        mockApiRestClient.put()
                .uri("/contas/{id}", conta.id())
                .body(atualizacao)
                .retrieve()
                .toBodilessEntity();
    }

    private ContaDados converterConta(ContaResponse conta) {

        return new ContaDados(
                conta.id(),
                conta.usuarioId(),
                conta.saldo(),
                conta.limiteDiario(),
                conta.limitePorTransacao()
        );
    }

    private record ContaResponse(
            String id,
            Long usuarioId,
            BigDecimal saldo,
            BigDecimal limiteDiario,
            BigDecimal limitePorTransacao
    ) {
    }

    private record ContaAtualizacao(
            Long usuarioId,
            BigDecimal saldo,
            BigDecimal limiteDiario,
            BigDecimal limitePorTransacao
    ) {
    }
}