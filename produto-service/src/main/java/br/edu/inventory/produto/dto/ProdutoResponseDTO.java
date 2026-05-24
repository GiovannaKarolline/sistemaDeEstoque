package br.edu.inventory.produto.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de resposta do Produto.
 * Inclui o preço convertido para USD via API externa (AwesomeAPI),
 * demonstrando integração com serviço externo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String sku;

    /** Preço original em Reais Brasileiros (BRL). */
    private BigDecimal precoBrl;

    /**
     * Preço convertido para Dólar Americano (USD).
     * Obtido via AwesomeAPI (economia.awesomeapi.com.br) — API externa pública.
     * Null caso a API esteja indisponível (circuit breaker ativo).
     */
    private BigDecimal precoUsd;

    /** Taxa de câmbio utilizada na conversão (BRL por 1 USD). */
    private BigDecimal taxaCambio;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
