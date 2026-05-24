package br.edu.inventory.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa a resposta do Produto Service.
 * Usado pelo Feign Client para desserializar os dados do produto.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoClientDTO {
    private Long id;
    private String nome;
    private String sku;
}
