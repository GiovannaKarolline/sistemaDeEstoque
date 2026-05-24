package br.edu.inventory.estoque.dto;

import br.edu.inventory.estoque.model.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoResponseDTO {
    private Long id;
    private Long produtoId;
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private LocalDateTime dataHora;
}
