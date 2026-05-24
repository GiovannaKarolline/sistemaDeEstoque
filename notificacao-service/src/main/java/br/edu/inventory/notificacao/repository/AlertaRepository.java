package br.edu.inventory.notificacao.repository;

import br.edu.inventory.notificacao.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByProdutoIdOrderByDataHoraGeracaoDesc(Long produtoId);
}
