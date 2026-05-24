package br.edu.inventory.estoque.repository;

import br.edu.inventory.estoque.model.Movimentacao;
import br.edu.inventory.estoque.model.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByProdutoIdOrderByDataHoraDesc(Long produtoId);

    /**
     * Calcula o total de entradas para um produto específico.
     */
    @Query("SELECT COALESCE(SUM(m.quantidade), 0) FROM Movimentacao m WHERE m.produtoId = :produtoId AND m.tipo = 'ENTRADA'")
    Integer sumEntradasByProdutoId(@Param("produtoId") Long produtoId);

    /**
     * Calcula o total de saídas para um produto específico.
     */
    @Query("SELECT COALESCE(SUM(m.quantidade), 0) FROM Movimentacao m WHERE m.produtoId = :produtoId AND m.tipo = 'SAIDA'")
    Integer sumSaidasByProdutoId(@Param("produtoId") Long produtoId);

}
