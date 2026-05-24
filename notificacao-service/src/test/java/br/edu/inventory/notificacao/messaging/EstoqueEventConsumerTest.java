package br.edu.inventory.notificacao.messaging;

import br.edu.inventory.notificacao.dto.EstoqueBaixoEventDTO;
import br.edu.inventory.notificacao.model.Alerta;
import br.edu.inventory.notificacao.repository.AlertaRepository;
import br.edu.inventory.notificacao.service.AlertaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoService — Testes do Consumer")
class EstoqueEventConsumerTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    @DisplayName("consumirEventoEstoqueBaixo() deve salvar um alerta no banco")
    void consumirEvento_deveSalvarAlerta() {
        // Arrange
        EstoqueEventConsumer consumer = new EstoqueEventConsumer(alertaService);
        EstoqueBaixoEventDTO evento = EstoqueBaixoEventDTO.builder()
                .produtoId(1L)
                .saldoAtual(5)
                .dataHoraOcorrencia(LocalDateTime.now())
                .build();

        // Act
        consumer.consumirEventoEstoqueBaixo(evento);

        // Assert
        verify(alertaRepository).save(any(Alerta.class));
    }
}
