package com.apigestaofinanceira.transacoes.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        return RestClient.builder()
                .baseUrl(ms1BaseUrl)
                .build();
    }

    @Bean
    public RestClient mockApiRestClient() {
        return RestClient.builder()
                .baseUrl(mockApiBaseUrl)
                .build();
    }

    @Bean
    public RestClient brasilApiRestClient() {
        return RestClient.builder()
                .baseUrl(brasilApiBaseUrl)
                .build();
    }
}