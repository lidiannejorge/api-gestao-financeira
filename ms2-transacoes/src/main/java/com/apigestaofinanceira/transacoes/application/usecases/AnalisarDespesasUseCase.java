package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.model.StatusTransacao;
import com.apigestaofinanceira.transacoes.domain.model.TipoTransacao;
import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalisarDespesasUseCase {

    // Só saídas aprovadas contam como despesa.
    private static final Set<TipoTransacao> TIPOS_DESPESA = Set.of(
            TipoTransacao.SAQUE,
            TipoTransacao.TRANSFERENCIA,
            TipoTransacao.COMPRA
    );

    private static final DateTimeFormatter FORMATO_MES =
            DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransacaoRepositoryPort transacaoRepository;

    public AnalisarDespesasUseCase(
            TransacaoRepositoryPort transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public Resultado executar(
            Long usuarioId, LocalDate inicio, LocalDate fim) {

        // Período padrão: mês atual, quando não informado.
        LocalDate dataInicio = inicio != null
                ? inicio
                : YearMonth.now().atDay(1);

        LocalDate dataFim = fim != null
                ? fim
                : YearMonth.now().atEndOfMonth();

        LocalDateTime inicioDateTime = dataInicio.atStartOfDay();
        LocalDateTime fimDateTime = dataFim.atTime(LocalTime.MAX);

        List<Transacao> transacoes = transacaoRepository.buscarComFiltros(
                        usuarioId, inicioDateTime, fimDateTime, null)
                .stream()
                .filter(t -> t.getStatus() == StatusTransacao.APROVADA)
                .filter(t -> TIPOS_DESPESA.contains(t.getTipo()))
                .toList();

        BigDecimal totalGeral = transacoes.stream()
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> porCategoria = transacoes.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria() != null
                                ? t.getCategoria()
                                : "Sem categoria",
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transacao::getValor,
                                BigDecimal::add)
                ));

        Map<LocalDate, BigDecimal> porDia = transacoes.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDataHora().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transacao::getValor,
                                BigDecimal::add)
                ));

        Map<String, BigDecimal> porMes = transacoes.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDataHora().toLocalDate()
                                .format(FORMATO_MES),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transacao::getValor,
                                BigDecimal::add)
                ));

        return new Resultado(
                dataInicio, dataFim, totalGeral,
                porCategoria, porDia, porMes
        );
    }

    public record Resultado(
            LocalDate inicio,
            LocalDate fim,
            BigDecimal totalGeral,
            Map<String, BigDecimal> porCategoria,
            Map<LocalDate, BigDecimal> porDia,
            Map<String, BigDecimal> porMes
    ) {
    }
}