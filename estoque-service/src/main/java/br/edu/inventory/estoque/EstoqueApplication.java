package br.edu.inventory.estoque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.jms.annotation.EnableJms;

/**
 * Microsserviço de Estoque.
 *
 * <p>Responsabilidades:
 * - Gerenciar entradas e saídas de estoque.
 * - Manter o saldo de produtos.
 * - Publicar eventos de estoque baixo.
 *
 * <p>Anotações:
 * - {@link EnableFeignClients}: Ativa o client HTTP Feign para comunicação
 *   síncrona com o Produto Service (Smart Endpoints).
 * - {@link EnableJms}: Ativa suporte a mensageria JMS para comunicação
 *   assíncrona (Event-Driven).
 */
@SpringBootApplication
@EnableFeignClients
@EnableJms
public class EstoqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueApplication.class, args);
    }
}
