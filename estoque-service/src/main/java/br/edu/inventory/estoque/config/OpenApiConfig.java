package br.edu.inventory.estoque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI estoqueServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Estoque Service API")
                        .description("Microsserviço de gestão de entradas, saídas e saldo de estoque.")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipe Acadêmica")));
    }
}
