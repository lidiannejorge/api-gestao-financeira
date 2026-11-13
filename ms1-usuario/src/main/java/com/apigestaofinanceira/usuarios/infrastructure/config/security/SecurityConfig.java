package com.apigestaofinanceira.usuarios.infrastructure.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TitularidadeFilter titularidadeFilter;
    private final UsuarioAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(
            TitularidadeFilter titularidadeFilter,
            UsuarioAuthenticationEntryPoint authenticationEntryPoint) {
        this.titularidadeFilter = titularidadeFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UsuarioAuthenticationProvider usuarioAuthenticationProvider) {
        return new ProviderManager(List.of(usuarioAuthenticationProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentação fica aberta.
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Cadastro, importação em massa e autenticação
                        // precisam continuar abertos: sem eles, ninguém
                        // consegue se cadastrar nem o MS2 consegue
                        // validar credenciais via Basic Auth.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/importar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/autenticar").permitAll()
                        // Usada pelo MS2 para validar se um usuário/destinatário
                        // existe antes de criar uma transação. Não expõe dados
                        // pessoais (nome, email) - só confirma existência.
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/*/existe").permitAll()
                        // Demais rotas (listar, buscar, atualizar, excluir)
                        // exigem usuário autenticado.
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                // Depois que o Basic Auth autentica o usuário, confere
                // se ele não está tentando acessar/alterar a conta de
                // outro usuário via /api/usuarios/{id}.
                .addFilterAfter(titularidadeFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    // Mantido desabilitado por padrão, igual ao MS2, até que a
    // proteção seja validada em todos os fluxos (cadastro, CSV,
    // listagem e MS2 chamando /autenticar).
    @Bean
    public FilterRegistrationBean<TitularidadeFilter> titularidadeFilterRegistration(
            TitularidadeFilter filter) {

        FilterRegistrationBean<TitularidadeFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }
}
