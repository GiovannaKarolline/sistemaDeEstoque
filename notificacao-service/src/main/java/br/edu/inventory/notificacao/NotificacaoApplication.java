package br.edu.inventory.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Microsserviço de Notificação.
 *
 * <p>Responsabilidade: Consumir eventos assíncronos via broker de mensagens
 * e gerar alertas no sistema.
 */
@SpringBootApplication
@EnableJms
public class NotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacaoApplication.class, args);
    }
}
