package br.edu.inventory.produto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuração do WebClient para consumo de APIs externas.
 *
 * <p>O WebClient é o cliente HTTP reativo do Spring, adequado para
 * chamadas não-bloqueantes a serviços externos como a AwesomeAPI.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
