package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.controller;

import java.net.URI;
import com.apigestaofinanceira.transacoes.application.usecases.*;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.AnaliseDespesasResponseDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.CreateTransacaoRequestDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper.TransacaoWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Transações")
@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final CreateTransacaoUseCase createUseCase;
    private final FindTransacaoUseCase findUseCase;
    private final ListTransacoesUseCase listUseCase;
    private final TransacaoWebMapper mapper;
    private final ContaClientPort contaClient;
    private final AnalisarDespesasUseCase analisarDespesasUseCase;
    private final GerarRelatorioCsvUseCase gerarRelatorioCsvUseCase;

    public TransacaoController(
            CreateTransacaoUseCase createUseCase,
            FindTransacaoUseCase findUseCase,
            ListTransacoesUseCase listUseCase,
            TransacaoWebMapper mapper,
            ContaClientPort contaClient,
            AnalisarDespesasUseCase analisarDespesasUseCase,
            GerarRelatorioCsvUseCase gerarRelatorioCsvUseCase) {

        this.createUseCase = createUseCase;
        this.findUseCase = findUseCase;
        this.listUseCase = listUseCase;
        this.mapper = mapper;
        this.contaClient = contaClient;
        this.analisarDespesasUseCase = analisarDespesasUseCase;
        this.gerarRelatorioCsvUseCase = gerarRelatorioCsvUseCase;
    }

    @Operation(summary = "Cria uma transação (depósito, saque, transferência ou compra)")
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody CreateTransacaoRequestDTO dto,
            @RequestHeader("X-Usuario-Id") Long usuarioId) {

        Transacao transacao = mapper.toDomain(dto);
        transacao.setUsuarioId(usuarioId);

        Transacao criada = createUseCase.executar(transacao);

        TransacaoResponseDTO response = mapper.toResponse(criada);

        URI location = URI.create("/api/transacoes/" + criada.getId());

        return ResponseEntity
                .created(location)
                .body(response);
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

    @Operation(summary = "Analisa as despesas do usuário por categoria, dia e mês em um período")
    @GetMapping("/despesas")
    public ResponseEntity<AnaliseDespesasResponseDTO> analisarDespesas(
            @RequestHeader("X-Usuario-Id") Long usuarioId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate fim) {

        AnalisarDespesasUseCase.Resultado resultado =
                analisarDespesasUseCase.executar(usuarioId, inicio, fim);

        List<AnaliseDespesasResponseDTO.ResumoCategoria> porCategoria =
                new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entrada
                : resultado.porCategoria().entrySet()) {

            porCategoria.add(
                    new AnaliseDespesasResponseDTO.ResumoCategoria(
                            entrada.getKey(), entrada.getValue())
            );
        }

        AnaliseDespesasResponseDTO resposta = new AnaliseDespesasResponseDTO(
                resultado.inicio(),
                resultado.fim(),
                resultado.totalGeral(),
                porCategoria,
                resultado.porDia(),
                resultado.porMes()
        );

        return ResponseEntity.ok(resposta);
    }

    @Operation(summary = "Gera um relatório em CSV com as transações do usuário autenticado")
    @GetMapping("/relatorio")
    public ResponseEntity<byte[]> gerarRelatorioCsv(
            @RequestHeader("X-Usuario-Id") Long usuarioId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy")
            LocalDate fim) {

        LocalDateTime dataInicio = inicio != null
                ? inicio.atStartOfDay()
                : null;

        LocalDateTime dataFim = fim != null
                ? fim.atTime(LocalTime.MAX)
                : null;

        byte[] csv = gerarRelatorioCsvUseCase.executar(
                usuarioId, dataInicio, dataFim);

        ContentDisposition disposition = ContentDisposition
                .attachment()
                .filename("relatorio-transacoes.csv")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}