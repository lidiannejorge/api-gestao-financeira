package com.apigestaofinanceira.transacoes.application.usecases;

import com.apigestaofinanceira.transacoes.domain.model.Transacao;
import com.apigestaofinanceira.transacoes.domain.ports.output.TransacaoRepositoryPort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GerarRelatorioCsvUseCase {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final TransacaoRepositoryPort transacaoRepository;

    public GerarRelatorioCsvUseCase(
            TransacaoRepositoryPort transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public byte[] executar(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {

        List<Transacao> transacoes = transacaoRepository.buscarComFiltros(
                usuarioId, inicio, fim, null
        );

        StringBuilder csv = new StringBuilder();

        // BOM para o Excel em português reconhecer o UTF-8 e não
        // quebrar os acentos. Separador ";" porque é o que o Excel
        // em pt-BR usa como padrão (a vírgula é o separador decimal).
        csv.append('\uFEFF');

        csv.append("Data;Tipo;Categoria;Descricao;Valor (R$);Status;Motivo\n");

        for (Transacao transacao : transacoes) {

            csv.append(escapar(transacao.getDataHora().format(FORMATO_DATA)));
            csv.append(';');
            csv.append(escapar(transacao.getTipo().name()));
            csv.append(';');
            csv.append(escapar(transacao.getCategoria()));
            csv.append(';');
            csv.append(escapar(transacao.getDescricao()));
            csv.append(';');
            csv.append(transacao.getValor().toString().replace('.', ','));
            csv.append(';');
            csv.append(escapar(transacao.getStatus().name()));
            csv.append(';');
            csv.append(escapar(transacao.getMotivoRejeicao()));
            csv.append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapar(String valor) {

        if (valor == null) {
            return "";
        }

        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }

        return valor;
    }
}