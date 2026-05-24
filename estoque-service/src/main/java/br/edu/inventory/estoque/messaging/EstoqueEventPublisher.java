package br.edu.inventory.estoque.messaging;

import br.edu.inventory.estoque.dto.EstoqueBaixoEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher de eventos JMS (Artemis/ActiveMQ).
 *
 * <p>Princípio Martin Fowler (Asynchronous Messaging): serviços se comunicam
 * assincronamente via broker de mensagens para eventos que não requerem
 * resposta imediata (ex: notificações).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EstoqueEventPublisher {

    private final JmsTemplate jmsTemplate;
    public static final String FILA_ESTOQUE_BAIXO = "estoque.baixo.queue";

    /**
     * Envia um evento de estoque baixo para a fila.
     * O Notificação Service estará escutando esta fila.
     */
    public void publicarEstoqueBaixo(EstoqueBaixoEventDTO evento) {
        log.info("Publicando evento de estoque baixo para o produtoId={}. Saldo atual: {}",
                evento.getProdutoId(), evento.getSaldoAtual());
        
        jmsTemplate.convertAndSend(FILA_ESTOQUE_BAIXO, evento);
    }
}
