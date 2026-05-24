package br.edu.inventory.produto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO que mapeia a resposta da AwesomeAPI para cotação USD-BRL.
 *
 * <p>Endpoint consumido: GET https://economia.awesomeapi.com.br/json/last/USD-BRL
 *
 * <p>Exemplo de resposta:
 * <pre>
 * {
 *   "USDBRL": {
 *     "code": "USD",
 *     "codein": "BRL",
 *     "bid": "5.12",
 *     "ask": "5.13",
 *     ...
 *   }
 * }
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AwesomeApiResponseDTO {

    @JsonProperty("USDBRL")
    private CotacaoDTO usdbrl;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CotacaoDTO {
        private String code;
        private String codein;
        private String name;
        /** Preço de compra (bid): quantidade de BRL por 1 USD. */
        private BigDecimal bid;
        /** Preço de venda (ask). */
        private BigDecimal ask;
        private String high;
        private String low;
    }
}
