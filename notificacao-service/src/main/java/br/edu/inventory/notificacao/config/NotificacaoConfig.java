package br.edu.inventory.notificacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class NotificacaoConfig {

    @Bean
    public OpenAPI notificacaoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notificação Service")
                        .version("1.0.0")
                        .description("Serviço consumidor de eventos do RabbitMQ/Artemis."));
    }

    /**
     * Configura a serialização das mensagens JMS para JSON.
     * Isso permite que os serviços troquem mensagens formatadas em JSON
     * de forma transparente, independente da linguagem.
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
