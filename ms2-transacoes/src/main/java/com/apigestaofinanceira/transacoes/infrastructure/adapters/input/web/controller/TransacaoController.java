package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.controller;

import com.apigestaofinanceira.transacoes.application.usecases.CreateTransacaoUseCase;
import com.apigestaofinanceira.transacoes.application.usecases.FindTransacaoUseCase;
import com.apigestaofinanceira.transacoes.application.usecases.ListTransacoesUseCase;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper.TransacaoWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "Transações")
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final CreateTransacaoUseCase createUseCase;
    private final FindTransacaoUseCase findUseCase;
    private final ListTransacoesUseCase listUseCase;
    private final TransacaoWebMapper mapper;
    private final ContaClientPort contaClient;

    public TransacaoController(
            CreateTransacaoUseCase createUseCase,
            FindTransacaoUseCase findUseCase,
            ListTransacoesUseCase listUseCase,
            TransacaoWebMapper mapper,
            ContaClientPort contaClient) {

        this.createUseCase = createUseCase;
        this.findUseCase = findUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
        this.contaClient = contaClient;
    }

    @Operation(summary = "Cria uma transação (depósito, saque, transferência ou compra)")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody CreateTransacaoRequestDTO dto,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        Transacao transacao = mapper.toDomain(dto);
        transacao.setUsuarioId(usuarioId);

        Transacao criada = createUseCase.executar(transacao);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(criada));
    }

    @Operation(summary = "Busca uma transação do usuário autenticado pelo identificador")
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(
            @PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        return findUseCase.executar(id, usuarioId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @Operation(summary = "Lista as transações do usuário autenticado, com filtro por período e tipo")
    @GetMapping("/usuario")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorUsuario(
            @RequestHeader("X-Usuario-Id") Long usuarioId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate fim,

            @RequestParam(required = false)
            TipoTransacao tipo) {

        LocalDateTime dataInicio = inicio != null
                ? inicio.atStartOfDay()
                : null;

        LocalDateTime dataFim = fim != null
                ? fim.atTime(LocalTime.MAX)
                : null;

        List<TransacaoResponseDTO> resposta = listUseCase.executar(
                        usuarioId, dataInicio, dataFim, tipo)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(resposta);
    }


    @Operation(summary = "Consulta o saldo atual do usuário autenticado")
    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> consultarSaldo(
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        ContaClientPort.ContaDados conta =
                contaClient.consultarConta(usuarioId);

        return ResponseEntity.ok(conta.saldo());
    }
}
