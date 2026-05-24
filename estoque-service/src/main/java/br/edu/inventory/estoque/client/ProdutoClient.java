package br.edu.inventory.estoque.client;

import br.edu.inventory.estoque.dto.ProdutoClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client para comunicação síncrona com o Produto Service.
 *
 * <p>Princípio Martin Fowler (Smart Endpoints): o Produto Service expõe API REST
 * clara e este cliente a consome.
 *
 * <p>A URL é resolvida via API Gateway ou diretamente. Neste caso simplificado
 * para ambiente de desenvolvimento sem Service Discovery (Eureka),
 * apontamos diretamente para a porta do Produto Service.
 */
@FeignClient(name = "produto-service", url = "${produto-service.url:http://localhost:8081/produtos}")
public interface ProdutoClient {

    /**
     * Busca um produto pelo ID.
     * Retorna o DTO com dados do produto ou lança FeignException se não encontrado (ex: 404).
     */
    @GetMapping("/{id}")
    ProdutoClientDTO buscarProdutoPorId(@PathVariable("id") Long id);
}
