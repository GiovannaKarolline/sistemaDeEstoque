package br.edu.inventory.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento DTO enviado para a fila quando o estoque atinge nível crítico.
 * Implementa Serializable para que o JMS possa converter em mensagem (ObjectMessage ou JSON).
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
