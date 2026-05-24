package br.edu.inventory.notificacao.messaging;

import br.edu.inventory.notificacao.dto.EstoqueBaixoEventDTO;
import br.edu.inventory.notificacao.service.AlertaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de eventos JMS.
 *
 * <p>Ouve a fila "estoque.baixo.queue" e aciona o serviço de alertas.
 * Comunicação Assíncrona via Mensageria (Martin Fowler).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EstoqueEventConsumer {

    private final AlertaService alertaService;
    private static final String FILA_ESTOQUE_BAIXO = "estoque.baixo.queue";

    @JmsListener(destination = FILA_ESTOQUE_BAIXO)
    public void consumirEventoEstoqueBaixo(EstoqueBaixoEventDTO evento) {
        log.info("Evento recebido na fila {}: Produto {}, Saldo {}",
                FILA_ESTOQUE_BAIXO, evento.getProdutoId(), evento.getSaldoAtual());
        
        alertaService.registrarAlertaEstoqueBaixo(evento);
    }
}
