package com.apigestaofinanceira.transacoes.domain.ports.output;

public interface UsuarioClientPort {

    boolean usuarioExiste(Long usuarioId);

}