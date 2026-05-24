package br.edu.inventory.produto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração OpenAPI 3 / Swagger UI.
 *
 * <p>Princípio Susan Fowler (Documentação): todos os microsserviços
 * devem ter documentação de API acessível e atualizada.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI produtoServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Produto Service API")
                        .description("""
                                Microsserviço responsável pelo gerenciamento de produtos do estoque.
                                
                                **API Externa integrada**: AwesomeAPI (economia.awesomeapi.com.br)
                                — retorna cotação USD-BRL em tempo real para conversão de preços.
                                
                                **Arquitetura**: Microsserviços seguindo Martin Fowler (2014) e Susan Fowler (2016).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@edu.br"))
                        .license(new License()
                                .name("Uso Acadêmico")));
    }
}
