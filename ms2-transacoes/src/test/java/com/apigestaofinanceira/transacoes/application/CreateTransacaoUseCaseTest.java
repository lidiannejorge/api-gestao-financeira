package com.apigestaofinanceira.transacoes.application;

import com.apigestaofinanceira.transacoes.application.usecases.CreateTransacaoUseCase;
import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.ContaClientPort;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import com.apigestaofinanceira.transacoes.domain.ports.output.UsuarioClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CreateTransacaoUseCaseTest {

    @Mock
    private TransacaoRepositoryPort transacaoRepository;

    @Mock
    private UsuarioClientPort usuarioClient;

    @Mock
    private ContaClientPort contaClient;

    private CreateTransacaoUseCase createTransacaoUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createTransacaoUseCase = new CreateTransacaoUseCase(
                transacaoRepository, usuarioClient, contaClient,
                Clock.systemDefaultZone());

        when(usuarioClient.usuarioExiste(anyLong())).thenReturn(true);

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transacaoRepository.buscarPorUsuarioId(anyLong()))
                .thenReturn(List.of());
    }

    private ContaClientPort.ContaDados contaComSaldo(BigDecimal saldo) {
        return new ContaClientPort.ContaDados(
                "1", 1L, saldo,
                new BigDecimal("1000"),
                new BigDecimal("500"));
    }

    // 1. Saque maior que o saldo deve ser rejeitado e gravado.
    @Test
    void deveRejeitarSaqueMaiorQueOSaldo() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("200"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("100")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.REJEITADA, resultado.getStatus());
        assertEquals("Saldo insuficiente.", resultado.getMotivoRejeicao());

        verify(transacaoRepository).salvar(transacao);
        verify(contaClient, never()).atualizarSaldo(anyLong(), any());
    }

    // 2. Compra dentro do saldo e dos limites deve ser aprovada.
    @Test
    void deveAprovarCompraDentroDoSaldoELimites() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("50"));
        transacao.setCategoria("Mercado");
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());
        assertNull(resultado.getMotivoRejeicao());

        verify(transacaoRepository).salvar(transacao);
        verify(contaClient).atualizarSaldo(
                1L, new BigDecimal("950"));
    }

    // 3. Compra acima do limite por transação deve ser rejeitada.
    @Test
    void deveRejeitarCompraAcimaDoLimitePorTransacao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("600"));
        transacao.setCategoria("Eletrônicos");
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.REJEITADA, resultado.getStatus());
        assertEquals(
                "O valor ultrapassa o limite por transação.",
                resultado.getMotivoRejeicao());

        verify(transacaoRepository).salvar(transacao);
        verify(contaClient, never()).atualizarSaldo(anyLong(), any());
    }

    // 4. Depósito não deve validar limites.
    @Test
    void depositoNaoDeveValidarLimites() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setValor(new BigDecimal("10000"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("0")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());

        verify(contaClient).atualizarSaldo(
                1L, new BigDecimal("10000"));
        verify(transacaoRepository).salvar(transacao);
    }

    // 5. Transferência exige destinatário.
    @Test
    void transferenciaSemDestinatarioDeveLancarExcecao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        verify(transacaoRepository, never()).salvar(any());
    }

    // 6. Compra sem categoria deve lançar exceção.
    @Test
    void compraSemCategoriaDeveLancarExcecao() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.COMPRA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        verify(transacaoRepository, never()).salvar(any());
    }

    // 7. Saque aprovado deve atualizar o saldo.
    @Test
    void deveAprovarSaqueEAtualizarSaldo() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("200"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());

        verify(contaClient).atualizarSaldo(
                1L, new BigDecimal("800"));

        verify(transacaoRepository).salvar(transacao);
    }

    // 8. Transferência deve atualizar os saldos das duas contas.
    @Test
    void deveAprovarTransferenciaEAtualizarAsDuasContas() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);
        transacao.setUsuarioDestinoId(2L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        when(contaClient.consultarConta(2L))
                .thenReturn(new ContaClientPort.ContaDados(
                        "2", 2L,
                        new BigDecimal("500"),
                        new BigDecimal("1000"),
                        new BigDecimal("500")));

        Transacao resultado = createTransacaoUseCase.executar(transacao);

        assertEquals(StatusTransacao.APROVADA, resultado.getStatus());

        verify(contaClient).atualizarSaldo(
                1L, new BigDecimal("900"));

        verify(contaClient).atualizarSaldo(
                2L, new BigDecimal("600"));

        verify(transacaoRepository).salvar(transacao);
    }

    // 9. Limite diário excedido deve rejeitar e gravar.
    @Test
    void deveRejeitarTransacaoQuandoLimiteDiarioForExcedido() {

        LocalDateTime dataHora = LocalDateTime.of(
                2026, 11, 1, 10, 0);

        Transacao anterior = new Transacao();
        anterior.setTipo(TipoTransacao.COMPRA);
        anterior.setValor(new BigDecimal("900"));
        anterior.setUsuarioId(1L);
        anterior.setStatus(StatusTransacao.APROVADA);
        anterior.setDataHora(dataHora);

        when(transacaoRepository.buscarPorUsuarioId(1L))
                .thenReturn(List.of(anterior));

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("2000")));

        Transacao novaTransacao = new Transacao();
        novaTransacao.setTipo(TipoTransacao.COMPRA);
        novaTransacao.setValor(new BigDecimal("200"));
        novaTransacao.setCategoria("Mercado");
        novaTransacao.setUsuarioId(1L);
        novaTransacao.setDataHora(dataHora);

        Transacao resultado =
                createTransacaoUseCase.executar(novaTransacao);

        assertEquals(StatusTransacao.REJEITADA, resultado.getStatus());

        assertEquals(
                "O limite diário foi ultrapassado.",
                resultado.getMotivoRejeicao());

        verify(transacaoRepository).salvar(novaTransacao);
        verify(contaClient, never()).atualizarSaldo(anyLong(), any());
    }

    // 10. Falha na MockAPI não pode aprovar nem gravar a transação.
    @Test
    void deveInterromperTransacaoQuandoMockApiFalhar() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenThrow(new RuntimeException("MockAPI indisponível"));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        assertEquals("MockAPI indisponível", excecao.getMessage());

        verify(transacaoRepository, never()).salvar(any());
        verify(contaClient, never()).atualizarSaldo(anyLong(), any());
    }

    // 11. Se salvar a transação falhar após o saque já ter debitado o
    // saldo, o saldo original deve ser restaurado (compensação).
    @Test
    void deveCompensarSaldoQuandoSalvarSaqueFalhar() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("200"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenThrow(new RuntimeException("Falha ao salvar no banco"));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        assertEquals(
                "Transação não registrada. O saldo foi revertido.",
                excecao.getMessage());

        // Debita o saldo (1000 -> 800) e depois compensa de volta (800 -> 1000).
        verify(contaClient).atualizarSaldo(1L, new BigDecimal("800"));
        verify(contaClient).atualizarSaldo(1L, new BigDecimal("1000"));
    }

    // 12. Se salvar falhar e a compensação do saldo também falhar, a
    // exceção original deve ser propagada com o erro de compensação
    // suprimido, sinalizando a necessidade de conciliação manual.
    @Test
    void deveLancarErroDeConciliacaoQuandoCompensacaoDoSaqueTambemFalhar() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setValor(new BigDecimal("200"));
        transacao.setUsuarioId(1L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenThrow(new RuntimeException("Falha ao salvar no banco"));

        doThrow(new RuntimeException("MockAPI indisponível"))
                .when(contaClient)
                .atualizarSaldo(1L, new BigDecimal("1000"));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        assertTrue(excecao.getMessage().contains("conciliação manual"));
        assertEquals(1, excecao.getSuppressed().length);
    }

    // 13. Se salvar a transferência falhar após débito e crédito já
    // terem ocorrido, os saldos de origem e destino devem ser
    // revertidos (compensação dupla).
    @Test
    void deveCompensarAmbosOsSaldosQuandoSalvarTransferenciaFalhar() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);
        transacao.setUsuarioDestinoId(2L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        when(contaClient.consultarConta(2L))
                .thenReturn(new ContaClientPort.ContaDados(
                        "2", 2L,
                        new BigDecimal("500"),
                        new BigDecimal("1000"),
                        new BigDecimal("500")));

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenThrow(new RuntimeException("Falha ao salvar no banco"));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        assertEquals(
                "Transferência não registrada. Os saldos foram revertidos.",
                excecao.getMessage());

        // Débito na origem (1000 -> 900) e crédito no destino (500 -> 600).
        verify(contaClient).atualizarSaldo(1L, new BigDecimal("900"));
        verify(contaClient).atualizarSaldo(2L, new BigDecimal("600"));

        // Compensação: origem e destino voltam ao saldo original.
        verify(contaClient).atualizarSaldo(1L, new BigDecimal("1000"));
        verify(contaClient).atualizarSaldo(2L, new BigDecimal("500"));
    }

    // 14. Se salvar a transferência falhar e a compensação de algum
    // dos saldos também falhar, a exceção original deve ser propagada
    // com o erro de compensação suprimido.
    @Test
    void deveLancarErroDeConciliacaoQuandoCompensacaoDaTransferenciaTambemFalhar() {

        Transacao transacao = new Transacao();
        transacao.setTipo(TipoTransacao.TRANSFERENCIA);
        transacao.setValor(new BigDecimal("100"));
        transacao.setUsuarioId(1L);
        transacao.setUsuarioDestinoId(2L);

        when(contaClient.consultarConta(1L))
                .thenReturn(contaComSaldo(new BigDecimal("1000")));

        when(contaClient.consultarConta(2L))
                .thenReturn(new ContaClientPort.ContaDados(
                        "2", 2L,
                        new BigDecimal("500"),
                        new BigDecimal("1000"),
                        new BigDecimal("500")));

        when(transacaoRepository.salvar(any(Transacao.class)))
                .thenThrow(new RuntimeException("Falha ao salvar no banco"));

        doThrow(new RuntimeException("MockAPI indisponível"))
                .when(contaClient)
                .atualizarSaldo(1L, new BigDecimal("1000"));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> createTransacaoUseCase.executar(transacao)
        );

        assertTrue(excecao.getMessage().contains("conciliação manual"));
        assertEquals(1, excecao.getSuppressed().length);
    }
}
