package br.edu.inventory.notificacao.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private Integer saldoRegistrado;

    @Column(nullable = false)
    private LocalDateTime dataHoraGeracao;

    @Column(nullable = false)
    private String mensagem;
}
