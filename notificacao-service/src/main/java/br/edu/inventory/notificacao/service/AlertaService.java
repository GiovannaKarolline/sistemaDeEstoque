package br.edu.inventory.notificacao.service;

import br.edu.inventory.notificacao.dto.EstoqueBaixoEventDTO;
import br.edu.inventory.notificacao.model.Alerta;
import br.edu.inventory.notificacao.repository.AlertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final AlertaRepository alertaRepository;

    @Transactional
    public void registrarAlertaEstoqueBaixo(EstoqueBaixoEventDTO evento) {
        log.info("Processando evento de estoque baixo para o produto {}", evento.getProdutoId());

        Alerta alerta = Alerta.builder()
                .produtoId(evento.getProdutoId())
                .saldoRegistrado(evento.getSaldoAtual())
                .dataHoraGeracao(LocalDateTime.now())
                .mensagem(String.format("Atenção! Estoque do produto %d atingiu nível crítico: %d unidades.",
                        evento.getProdutoId(), evento.getSaldoAtual()))
                .build();

        alertaRepository.save(alerta);
        log.info("Alerta salvo no banco de dados com ID {}", alerta.getId());
    }

    public List<Alerta> listarTodos() {
        return alertaRepository.findAll();
    }

    public List<Alerta> listarPorProduto(Long produtoId) {
        return alertaRepository.findByProdutoIdOrderByDataHoraGeracaoDesc(produtoId);
    }
}
