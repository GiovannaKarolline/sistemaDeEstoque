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
        // Arrange
        mockProdutoExistente(1L);
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(50);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(20);

        // Act
        SaldoResponseDTO saldo = estoqueService.consultarSaldo(1L);

        // Assert
        assertThat(saldo.getProdutoId()).isEqualTo(1L);
        assertThat(saldo.getSaldo()).isEqualTo(30); // 50 - 20
        verify(produtoClient).buscarProdutoPorId(1L);
    }

    @Test
    @DisplayName("registrarMovimentacao() de SAIDA deve falhar se o saldo for insuficiente")
    void registrarMovimentacao_saidaSemSaldo_deveLancarException() {
        // Arrange
        mockProdutoExistente(1L);
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(10);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(5);
        // Saldo atual = 5

        MovimentacaoRequestDTO request = MovimentacaoRequestDTO.builder()
                .produtoId(1L)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(10) // Solicitando 10, tem 5
                .build();

        // Act & Assert
        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(request))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(movimentacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publicarEstoqueBaixo(any());
    }

    @Test
    @DisplayName("registrarMovimentacao() deve publicar evento se o novo saldo ficar < 10")
    void registrarMovimentacao_saldoFicouBaixo_devePublicarEvento() {
        // Arrange
        mockProdutoExistente(1L);
        
        // Antes de salvar (Para validar se pode sair): Saldo = 15
        when(movimentacaoRepository.sumEntradasByProdutoId(1L)).thenReturn(15, 15);
        when(movimentacaoRepository.sumSaidasByProdutoId(1L)).thenReturn(0, 10); // Depois de salvar a saída será 10. Novo saldo = 5

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

        // Act
        MovimentacaoResponseDTO response = estoqueService.registrarMovimentacao(request);

        // Assert
        assertThat(response.getId()).isEqualTo(100L);
        verify(movimentacaoRepository).save(any());
        // Deve ter publicado evento, pois o saldo novo (5) < 10
        verify(eventPublisher).publicarEstoqueBaixo(any(EstoqueBaixoEventDTO.class));
    }
}
