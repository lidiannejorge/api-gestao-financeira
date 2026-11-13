package com.apigestaofinanceira.transacoes.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TitularidadeFilter titularidadeFilter;
    private final Ms1AuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(TitularidadeFilter titularidadeFilter, Ms1AuthenticationEntryPoint authenticationEntryPoint) {
        this.titularidadeFilter = titularidadeFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            Ms1AuthenticationProvider ms1AuthenticationProvider) {
        return new ProviderManager(List.of(ms1AuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentação fica aberta, como o PO pediu.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Todo o resto (transações, saldo, despesas, relatório)
                        // exige usuário autenticado.
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                // Depois que o Basic Auth autentica o usuário, confere
                // se ele não está tentando acessar dados de outro
                // usuário via X-Usuario-Id.
                .addFilterAfter(
                        titularidadeFilter, BasicAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public FilterRegistrationBean<TitularidadeFilter> titularidadeFilterRegistration(
            TitularidadeFilter filter) {

        FilterRegistrationBean<TitularidadeFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }
}