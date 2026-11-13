package com.apigestaofinanceira.transacoes.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class Ms1ClientConfig {

    @Value("${ms1.base-url}")
    private String ms1BaseUrl;

    @Value("${mockapi.base-url}")
    private String mockApiBaseUrl;

    @Value("${brasilapi.base-url}")
    private String brasilApiBaseUrl;

    @Bean
    public RestClient ms1RestClient() {

        // Timeout curto para evitar que uma falha ou lentidão do MS1
        // deixe o MS2 esperando indefinidamente numa autenticação.
        return RestClient.builder()
                .baseUrl(ms1BaseUrl)
                .requestFactory(criarRequestFactory(3000, 5000))
                .build();
    }

    @Bean
    public RestClient mockApiRestClient() {

        // Mesmo motivo do ms1RestClient: a API mock de contas é chamada
        // em toda transação (consulta e atualização de saldo), então
        // precisa de timeout para não travar o MS2 indefinidamente.
        return RestClient.builder()
                .baseUrl(mockApiBaseUrl)
                .requestFactory(criarRequestFactory(3000, 5000))
                .build();
    }

    @Bean
    public RestClient brasilApiRestClient() {

        // Timeout também aplicado aqui para evitar que uma lentidão da
        // BrasilAPI (câmbio) deixe requisições do MS2 pendentes.
        return RestClient.builder()
                .baseUrl(brasilApiBaseUrl)
                .requestFactory(criarRequestFactory(3000, 5000))
                .build();
    }

    private SimpleClientHttpRequestFactory criarRequestFactory(
            int connectTimeoutMs, int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        return requestFactory;
    }
}
