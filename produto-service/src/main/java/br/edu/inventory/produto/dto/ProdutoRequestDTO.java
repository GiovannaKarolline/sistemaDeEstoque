package br.edu.inventory.produto.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de entrada para criação e atualização de Produto.
 * Segue o padrão de separação DTO/Entity (Martin Fowler — Data Transfer Object pattern).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 150)
    private String nome;

    @Size(max = 500)
    private String descricao;

    @NotBlank(message = "SKU é obrigatório")
    private String sku;

    @NotNull(message = "Preço em BRL é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser positivo")
    private BigDecimal precoBrl;
}
