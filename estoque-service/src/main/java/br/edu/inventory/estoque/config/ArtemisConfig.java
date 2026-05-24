package br.edu.inventory.estoque.config;

import org.apache.activemq.artemis.core.config.Configuration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.stereotype.Component;

/**
 * Configuração do Broker de Mensagens Embutido (Embedded Artemis).
 *
 * <p>Para rodar o projeto sem dependências externas como o Docker,
 * o Estoque Service inicia um broker de mensagens internamente e
 * expõe a porta TCP 61616.
 *
 * <p>O Notificação Service se conectará a esta porta para consumir
 * os eventos de estoque baixo.
 */
@Component
public class ArtemisConfig implements ArtemisConfigurationCustomizer {

    @Override
    public void customize(Configuration configuration) {
        try {
            // Permite conexões externas na porta padrão do Artemis/ActiveMQ
            configuration.addAcceptorConfiguration("tcp", "tcp://0.0.0.0:61616");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao configurar broker Artemis", e);
        }
    }
}
