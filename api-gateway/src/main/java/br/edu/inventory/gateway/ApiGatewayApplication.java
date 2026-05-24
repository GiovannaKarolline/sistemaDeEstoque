package br.edu.inventory.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — Ponto único de entrada do sistema de microsserviços.
 *
 * <p>Princípio Martin Fowler: o Gateway centraliza o roteamento, mas não
 * contém lógica de negócio ("smart endpoints, dumb pipes").
 *
 * <p>Princípio Susan Fowler: expõe /actuator/health para monitoramento
 * e health checks externos.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
