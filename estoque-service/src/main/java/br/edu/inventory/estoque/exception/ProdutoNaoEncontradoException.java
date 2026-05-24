package br.edu.inventory.estoque.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProdutoNaoEncontradoException extends RuntimeException {

    public ProdutoNaoEncontradoException(Long produtoId) {
        super(String.format("Produto com ID %d não existe no Produto Service", produtoId));
    }
}
