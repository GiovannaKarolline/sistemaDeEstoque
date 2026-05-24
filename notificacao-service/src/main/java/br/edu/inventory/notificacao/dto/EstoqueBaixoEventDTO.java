package br.edu.inventory.notificacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento consumido da fila JMS.
 * O DTO deve ter as mesmas propriedades do publicado pelo Estoque Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstoqueBaixoEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long produtoId;
    private Integer saldoAtual;
    private LocalDateTime dataHoraOcorrencia;
}
