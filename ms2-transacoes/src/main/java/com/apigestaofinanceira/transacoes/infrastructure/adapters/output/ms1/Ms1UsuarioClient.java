package com.apigestaofinanceira.transacoes.infrastructure.adapters.output.ms1;

import com.apigestaofinanceira.transacoes.domain.ports.output.UsuarioClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class Ms1UsuarioClient implements UsuarioClientPort {

    private final RestClient ms1RestClient;

    public Ms1UsuarioClient(
            @Qualifier("ms1RestClient") RestClient ms1RestClient) {
        this.ms1RestClient = ms1RestClient;
    }

    @Override
    public boolean usuarioExiste(Long usuarioId) {
        try {
            ms1RestClient.get()
                    .uri("/api/usuarios/{id}", usuarioId)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }

            throw e;
        }
    }
}
