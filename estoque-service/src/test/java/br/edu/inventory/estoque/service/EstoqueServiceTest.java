package br.edu.inventory.estoque.service;

import br.edu.inventory.estoque.client.ProdutoClient;
import br.edu.inventory.estoque.dto.EstoqueBaixoEventDTO;
import br.edu.inventory.estoque.dto.MovimentacaoRequestDTO;
import br.edu.inventory.estoque.dto.MovimentacaoResponseDTO;
import br.edu.inventory.estoque.dto.ProdutoClientDTO;
import br.edu.inventory.estoque.dto.SaldoResponseDTO;
import br.edu.inventory.estoque.exception.EstoqueInsuficienteException;
import br.edu.inventory.estoque.messaging.EstoqueEventPublisher;
import br.edu.inventory.estoque.model.Movimentacao;
import br.edu.inventory.estoque.model.TipoMovimentacao;
import br.edu.inventory.estoque.repository.MovimentacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueService — Testes Unitários")
class EstoqueServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ProdutoClient produtoClient;

    @Mock
    private EstoqueEventPublisher eventPublisher;

    @InjectMocks
    private EstoqueService estoqueService;

    private void mockProdutoExistente(Long produtoId) {
        when(produtoClient.buscarProdutoPorId(produtoId))
                .thenReturn(new ProdutoClientDTO(produtoId, "Produto Teste", "SKU-TESTE"));
    }

    @Test
    @DisplayName("consultarSaldo() deve retornar o saldo correto baseado nas movimentações")
    void consultarSaldo_deveRetornarSaldoCorreto() {
        // Prepara
        mockProdutoExistente(1L);
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(50);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(20);

        // Executa
        SaldoResponseDTO saldo = estoqueService.consultarSaldo(1L);

        // Valida
        assertThat(saldo.getProdutoId()).isEqualTo(1L);
        assertThat(saldo.getSaldo()).isEqualTo(30); // Subtrai
        verify(produtoClient).buscarProdutoPorId(1L);
    }

    @Test
    @DisplayName("registrarMovimentacao() de SAIDA deve falhar se o saldo for insuficiente")
    void registrarMovimentacao_saidaSemSaldo_deveLancarException() {
        // Prepara
        mockProdutoExistente(1L);
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(10);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(5);
        // Saldo 5

        MovimentacaoRequestDTO request = MovimentacaoRequestDTO.builder()
                .produtoId(1L)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(10) // Falta 5
                .build();

        // Executa e valida
        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(request))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(movimentacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publicarEstoqueBaixo(any());
    }

    @Test
    @DisplayName("registrarMovimentacao() deve publicar evento se o novo saldo ficar < 10")
    void registrarMovimentacao_saldoFicouBaixo_devePublicarEvento() {
        // Prepara
        mockProdutoExistente(1L);
        
        // Saldo inicial 15
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(15, 15);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(0, 10); // Saldo final 5

        MovimentacaoRequestDTO request = MovimentacaoRequestDTO.builder()
                .produtoId(1L)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(10)
                .build();

        Movimentacao salva = Movimentacao.builder()
                .id(100L)
                .produtoId(1L)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(10)
                .build();

        when(movimentacaoRepository.save(any())).thenReturn(salva);

        // Executa
        MovimentacaoResponseDTO response = estoqueService.registrarMovimentacao(request);

        // Valida
        assertThat(response.getId()).isEqualTo(100L);
        verify(movimentacaoRepository).save(any());
        // Publica evento
        verify(eventPublisher).publicarEstoqueBaixo(any(EstoqueBaixoEventDTO.class));
    }
}
