package br.edu.inventory.estoque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(Long produtoId, Integer saldoAtual, Integer quantidadeDesejada) {
        super(String.format("Estoque insuficiente para o produto %d. Saldo atual: %d. Solicitado: %d",
                produtoId, saldoAtual, quantidadeDesejada));
    }
}
