package br.edu.inventory.produto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microsserviço de Produtos.
 *
 * <p>Responsabilidade única (Martin Fowler): gerenciar o ciclo de vida
 * dos produtos do estoque, incluindo consulta de preço em moeda estrangeira
 * via API externa (AwesomeAPI).
 *
 * <p>Banco de dados próprio (Martin Fowler — Decentralized Data Management):
 * H2 in-memory isolado dos demais serviços.
 */
@SpringBootApplication
public class ProdutoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdutoApplication.class, args);
    }
}
