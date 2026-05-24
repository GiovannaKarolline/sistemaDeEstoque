package br.edu.inventory.produto.repository;

import br.edu.inventory.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA do Produto.
 *
 * <p>Princípio Martin Fowler (Repository Pattern): abstrai o acesso
 * ao banco de dados, permitindo substituição de implementação sem
 * alterar a lógica de negócio.
 *
 * <p>Banco de dados próprio deste serviço (H2 in-memory),
 * garantindo isolamento (Decentralized Data Management — Martin Fowler).
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Verifica se já existe um produto com o mesmo SKU.
     * Utilizado na validação de unicidade durante criação/atualização.
     */
    Optional<Produto> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
