package br.edu.inventory.produto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando há tentativa de criar/atualizar produto
 * com um SKU já cadastrado no sistema.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SkuDuplicadoException extends RuntimeException {

    public SkuDuplicadoException(String sku) {
        super(String.format("Já existe um produto com o SKU='%s'", sku));
    }
}
