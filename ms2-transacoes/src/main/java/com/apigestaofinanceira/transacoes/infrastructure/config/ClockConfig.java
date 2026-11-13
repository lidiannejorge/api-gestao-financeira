package com.apigestaofinanceira.transacoes.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    // Garante que data e hora das transações usem o fuso de São Paulo,
    // independentemente do fuso configurado no sistema operacional/
    // container onde a JVM está rodando (que no Docker normalmente é UTC).
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
