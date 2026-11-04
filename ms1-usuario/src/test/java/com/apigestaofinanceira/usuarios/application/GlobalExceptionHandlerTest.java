package com.apigestaofinanceira.usuarios.application;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.EmailJaCadastradoException;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.GlobalExceptionHandler;
import com.apigestaofinanceira.usuarios.infrastructure.config.exception.UsuarioNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void deveRetornar404QuandoUsuarioNaoForEncontrado() {
        UsuarioNotFoundException exception =
                new UsuarioNotFoundException(10L);

        var resposta = handler.handleUsuarioNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals(404, resposta.getBody().get("status"));
        assertEquals(exception.getMessage(), resposta.getBody().get("message"));
        assertNotNull(resposta.getBody().get("timestamp"));
    }

    @Test
    void deveRetornar409QuandoEmailEstiverDuplicado() {
        EmailJaCadastradoException exception =
                new EmailJaCadastradoException("ana@gmail.com");

        var resposta = handler.handleEmailDuplicado(exception);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(409, resposta.getBody().get("status"));
        assertEquals(exception.getMessage(), resposta.getBody().get("message"));
    }

    @Test
    void deveRetornar400QuandoArgumentoForInvalido() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Arquivo inválido.");

        var resposta = handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(400, resposta.getBody().get("status"));
        assertEquals("Arquivo inválido.", resposta.getBody().get("message"));
    }

    @Test
    void deveRetornar400ComDetalhesDaValidacao() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "createUsuarioRequestDTO",
                "name",
                "O nome é obrigatório."
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var resposta = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(400, resposta.getBody().get("status"));

        Map<?, ?> erros = (Map<?, ?>) resposta.getBody().get("errors");

        assertEquals("O nome é obrigatório.", erros.get("name"));
        assertNotNull(resposta.getBody().get("timestamp"));
    }
}