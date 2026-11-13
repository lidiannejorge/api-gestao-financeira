package com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.controller;

import com.apigestaofinanceira.transacoes.application.usecases.*;
import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.dto.TransacaoResponseDTO;
import com.apigestaofinanceira.transacoes.infrastructure.adapters.input.web.mapper.TransacaoWebMapper;
import com.apigestaofinanceira.transacoes.infrastructure.config.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTransacaoUseCase createUseCase;

    @MockitoBean
    private FindTransacaoUseCase findUseCase;

    @MockitoBean
    private ListTransacoesUseCase listUseCase;

    @MockitoBean
    private TransacaoWebMapper mapper;

    @MockitoBean
    private ContaClientPort contaClient;

    @MockitoBean
    private AnalisarDespesasUseCase analisarDespesasUseCase;

    @MockitoBean
    private GerarRelatorioCsvUseCase gerarRelatorioCsvUseCase;

    private Transacao criarTransacao() {
        Transacao transacao = new Transacao();

        transacao.setId(1L);
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("150.90"));
        transacao.setDataHora(LocalDateTime.of(2026, 10, 31, 10, 30));
        transacao.setDescricao("Compra no supermercado");
        transacao.setCategoria("ALIMENTACAO");
        transacao.setUsuarioId(1L);
        transacao.setStatus(StatusTransacao.APROVADA);

        return transacao;
    }

    private TransacaoResponseDTO criarResposta() {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();

        dto.setId(1L);
        dto.setTipo(TipoTransacao.COMPRA);
        dto.setValor(new BigDecimal("150.90"));
        dto.setDataHora(LocalDateTime.of(2026, 10, 31, 10, 30));
        dto.setDescricao("Compra no supermercado");
        dto.setCategoria("ALIMENTACAO");
        dto.setUsuarioId(1L);
        dto.setStatus(StatusTransacao.APROVADA);

        return dto;
    }

    @Test
    void deveCriarTransacaoERetornar201() throws Exception {
        Transacao transacao = criarTransacao();

        when(mapper.toDomain(any())).thenReturn(transacao);
        when(createUseCase.executar(any())).thenReturn(transacao);
        when(mapper.toResponse(transacao)).thenReturn(criarResposta());

        mockMvc.perform(post("/api/transacoes")
                        .header("X-Usuario-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "COMPRA",
                                  "valor": 150.90,
                                  "descricao": "Compra no supermercado",
                                  "categoria": "ALIMENTACAO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("COMPRA"))
                .andExpect(jsonPath("$.status").value("APROVADA"));

        verify(createUseCase).executar(any(Transacao.class));
    }

    @Test
    void deveRetornar400QuandoDadosDaTransacaoForemInvalidos()
            throws Exception {

        mockMvc.perform(post("/api/transacoes")
                        .header("X-Usuario-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": null,
                                  "valor": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.tipo").exists())
                .andExpect(jsonPath("$.errors.valor").exists());

        verifyNoInteractions(createUseCase);
    }

    @Test
    void deveBuscarTransacaoPorIdERetornar200() throws Exception {
        Transacao transacao = criarTransacao();

        when(findUseCase.executar(1L, 1L))
                .thenReturn(Optional.of(transacao));

        when(mapper.toResponse(transacao))
                .thenReturn(criarResposta());

        mockMvc.perform(get("/api/transacoes/1")
                        .header("X-Usuario-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.valor").value(150.90));
    }

    @Test
    void deveRetornar404QuandoTransacaoNaoExistir() throws Exception {
        when(findUseCase.executar(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transacoes/999")
                        .header("X-Usuario-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarTransacoesComFiltrosERetornar200() throws Exception {
        Transacao transacao = criarTransacao();

        when(listUseCase.executar(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(TipoTransacao.COMPRA)
        )).thenReturn(List.of(transacao));

        when(mapper.toResponse(transacao))
                .thenReturn(criarResposta());

        mockMvc.perform(get("/api/transacoes/usuario")
                        .header("X-Usuario-Id", 1L)
                        .param("inicio", "01/10/2026")
                        .param("fim", "31/10/2026")
                        .param("tipo", "COMPRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("COMPRA"));

        verify(listUseCase).executar(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(TipoTransacao.COMPRA)
        );
    }

    @Test
    void deveRetornar400QuandoDataFinalForAnteriorAInicial()
            throws Exception {

        when(listUseCase.executar(
                anyLong(),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull()
        )).thenThrow(new IllegalArgumentException(
                "A data final não pode ser anterior à data inicial."
        ));

        mockMvc.perform(get("/api/transacoes/usuario")
                        .header("X-Usuario-Id", 1L)
                        .param("inicio", "31/10/2026")
                        .param("fim", "01/10/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deveConsultarSaldoERetornar200() throws Exception {
        ContaClientPort.ContaDados conta =
                mock(ContaClientPort.ContaDados.class);

        when(conta.saldo()).thenReturn(new BigDecimal("500.00"));
        when(contaClient.consultarConta(1L)).thenReturn(conta);

        mockMvc.perform(get("/api/transacoes/saldo")
                        .header("X-Usuario-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    void deveAnalisarDespesasERetornar200() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 10, 1);
        LocalDate fim = LocalDate.of(2026, 10, 31);

        AnalisarDespesasUseCase.Resultado resultado =
                new AnalisarDespesasUseCase.Resultado(
                        inicio,
                        fim,
                        new BigDecimal("150.90"),
                        Map.of("ALIMENTACAO", new BigDecimal("150.90")),
                        Map.of(
                                LocalDate.of(2026, 10, 31),
                                new BigDecimal("150.90")
                        ),
                        Map.of("2026-10", new BigDecimal("150.90"))
                );

        when(analisarDespesasUseCase.executar(1L, inicio, fim))
                .thenReturn(resultado);

        mockMvc.perform(get("/api/transacoes/despesas")
                        .header("X-Usuario-Id", 1L)
                        .param("inicio", "01/10/2026")
                        .param("fim", "31/10/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGeral").value(150.90))
                .andExpect(jsonPath("$.porCategoria[0].categoria")
                        .value("ALIMENTACAO"));
    }

    @Test
    void deveGerarRelatorioCsvERetornar200() throws Exception {
        byte[] csv = (
                "\uFEFFData;Tipo;Categoria;Descricao;Valor (R$);Status;Motivo\n"
                        + "31/10/2026 10:30:00;COMPRA;ALIMENTACAO;"
                        + "Compra;150,90;APROVADA;\n"
        ).getBytes(java.nio.charset.StandardCharsets.UTF_8);

        when(gerarRelatorioCsvUseCase.executar(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(csv);

        mockMvc.perform(get("/api/transacoes/relatorio")
                        .header("X-Usuario-Id", 1L)
                        .param("inicio", "01/10/2026")
                        .param("fim", "31/10/2026"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString(
                                "relatorio-transacoes.csv"
                        )
                ))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().bytes(csv));
    }
}
