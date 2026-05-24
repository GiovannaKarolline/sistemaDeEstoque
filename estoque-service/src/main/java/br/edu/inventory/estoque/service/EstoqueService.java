package br.edu.inventory.estoque.service;

import br.edu.inventory.estoque.client.ProdutoClient;
import br.edu.inventory.estoque.dto.EstoqueBaixoEventDTO;
import br.edu.inventory.estoque.dto.MovimentacaoRequestDTO;
import br.edu.inventory.estoque.dto.MovimentacaoResponseDTO;
import br.edu.inventory.estoque.dto.SaldoResponseDTO;
import br.edu.inventory.estoque.exception.EstoqueInsuficienteException;
import br.edu.inventory.estoque.exception.ProdutoNaoEncontradoException;
import br.edu.inventory.estoque.messaging.EstoqueEventPublisher;
import br.edu.inventory.estoque.model.Movimentacao;
import br.edu.inventory.estoque.model.TipoMovimentacao;
import br.edu.inventory.estoque.repository.MovimentacaoRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de negócio do Estoque.
 *
 * <p>Integra com Produto Service via Feign e publica eventos via JMS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EstoqueService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoClient produtoClient;
    private final EstoqueEventPublisher eventPublisher;

    private static final int LIMITE_ESTOQUE_BAIXO = 10;

    /**
     * Calcula o saldo atual de um produto.
     * Saldo = Total Entradas - Total Saídas
     */
    public SaldoResponseDTO consultarSaldo(Long produtoId) {
        log.info("Consultando saldo do produto {}", produtoId);
        validarProdutoExiste(produtoId);
        
        Integer saldo = calcularSaldoInterno(produtoId);
        return new SaldoResponseDTO(produtoId, saldo);
    }

    public List<MovimentacaoResponseDTO> listarMovimentacoes(Long produtoId) {
        validarProdutoExiste(produtoId);
        return movimentacaoRepository.findByProdutoIdOrderByDataHoraDesc(produtoId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra uma nova movimentação.
     * Se for SAIDA, verifica se há saldo suficiente.
     * Após a movimentação, se o saldo ficar menor que o limite, publica um evento.
     */
    @Transactional
    public MovimentacaoResponseDTO registrarMovimentacao(MovimentacaoRequestDTO dto) {
        log.info("Registrando movimentação do tipo {} para produto {}", dto.getTipo(), dto.getProdutoId());
        validarProdutoExiste(dto.getProdutoId());

        Integer saldoAtual = calcularSaldoInterno(dto.getProdutoId());

        if (dto.getTipo() == TipoMovimentacao.SAIDA && saldoAtual < dto.getQuantidade()) {
            log.warn("Estoque insuficiente. Saldo: {}, Solicitado: {}", saldoAtual, dto.getQuantidade());
            throw new EstoqueInsuficienteException(dto.getProdutoId(), saldoAtual, dto.getQuantidade());
        }

        Movimentacao movimentacao = Movimentacao.builder()
                .produtoId(dto.getProdutoId())
                .tipo(dto.getTipo())
                .quantidade(dto.getQuantidade())
                .build();

        Movimentacao salva = movimentacaoRepository.save(movimentacao);
        
        Integer novoSaldo = calcularSaldoInterno(dto.getProdutoId()); // Recalcula
        log.info("Novo saldo do produto {}: {}", dto.getProdutoId(), novoSaldo);

        if (novoSaldo < LIMITE_ESTOQUE_BAIXO) {
            EstoqueBaixoEventDTO evento = EstoqueBaixoEventDTO.builder()
                    .produtoId(dto.getProdutoId())
                    .saldoAtual(novoSaldo)
                    .dataHoraOcorrencia(LocalDateTime.now())
                    .build();
            eventPublisher.publicarEstoqueBaixo(evento);
        }

        return toResponseDTO(salva);
    }

    // Auxiliares

    private Integer calcularSaldoInterno(Long produtoId) {
        Integer totalEntradas = movimentacaoRepository.sumEntradasByProdutoId(produtoId);
        Integer totalSaidas = movimentacaoRepository.sumSaidasByProdutoId(produtoId);
        return totalEntradas - totalSaidas;
    }

    /**
     * Valida a existência do produto consultando o Produto Service via HTTP.
     * Utiliza Circuit Breaker.
     */
    @CircuitBreaker(name = "produtoService", fallbackMethod = "validarProdutoFallback")
    private void validarProdutoExiste(Long produtoId) {
        try {
            log.info("Validando produto {} no Produto Service", produtoId);
            produtoClient.buscarProdutoPorId(produtoId);
        } catch (FeignException.NotFound ex) {
            throw new ProdutoNaoEncontradoException(produtoId);
        }
    }

    /**
     * Fallback do Circuit Breaker: se o Produto Service estiver fora do ar,
     * permite a operação baseada apenas no ID, assumindo que existe.
     * Num cenário real, poderíamos ter um cache local ou recusar a operação.
     * Aqui, aplicamos degradação graciosa.
     */
    private void validarProdutoFallback(Long produtoId, Exception ex) {
        if (ex instanceof ProdutoNaoEncontradoException) {
            throw (ProdutoNaoEncontradoException) ex;
        }
        log.warn("Produto Service indisponível. Circuit Breaker ativo. Assumindo que produto {} é válido.", produtoId);
    }

    private MovimentacaoResponseDTO toResponseDTO(Movimentacao m) {
        return MovimentacaoResponseDTO.builder()
                .id(m.getId())
                .produtoId(m.getProdutoId())
                .tipo(m.getTipo())
                .quantidade(m.getQuantidade())
                .dataHora(m.getDataHora())
                .build();
    }
}
