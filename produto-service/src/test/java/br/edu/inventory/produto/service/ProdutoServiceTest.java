package br.edu.inventory.produto.service;

import br.edu.inventory.produto.dto.ProdutoRequestDTO;
import br.edu.inventory.produto.dto.ProdutoResponseDTO;
import br.edu.inventory.produto.exception.ProdutoNotFoundException;
import br.edu.inventory.produto.exception.SkuDuplicadoException;
import br.edu.inventory.produto.model.Produto;
import br.edu.inventory.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do ProdutoService.
 *
 * <p>Princípio Susan Fowler (Testabilidade): testes unitários isolados,
 * sem dependências externas, usando Mockito para substituir colaboradores.
 *
 * <p>Segue o padrão AAA (Arrange-Act-Assert) de Martin Fowler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService — Testes Unitários")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CambioService cambioService;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoExemplo;

    @BeforeEach
    void setUp() {
        produtoExemplo = Produto.builder()
                .id(1L)
                .nome("Teclado Mecânico")
                .descricao("Teclado mecânico ABNT2")
                .sku("TEC-001")
                .precoBrl(new BigDecimal("450.00"))
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("listarTodos() deve retornar lista de produtos")
    void listarTodos_deveRetornarLista() {
        // Arrange
        when(produtoRepository.findAll()).thenReturn(List.of(produtoExemplo));

        // Act
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Teclado Mecânico");
        assertThat(resultado.get(0).getSku()).isEqualTo("TEC-001");
        verify(produtoRepository).findAll();
        // listarTodos() NÃO deve chamar a API de câmbio (performance)
        verifyNoInteractions(cambioService);
    }

    @Test
    @DisplayName("buscarPorId() deve retornar produto com preço USD quando API está disponível")
    void buscarPorId_comApiDisponivel_deveRetornarComPrecoUsd() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExemplo));
        when(cambioService.buscarTaxaCambioUsdBrl()).thenReturn(new BigDecimal("5.00"));

        // Act
        ProdutoResponseDTO resultado = produtoService.buscarPorId(1L);

        // Assert
        assertThat(resultado.getPrecoBrl()).isEqualByComparingTo("450.00");
        assertThat(resultado.getPrecoUsd()).isEqualByComparingTo("90.0000"); // 450 / 5 = 90
        assertThat(resultado.getTaxaCambio()).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("buscarPorId() deve retornar produto sem preço USD quando API (circuit breaker) retorna null")
    void buscarPorId_comCircuitBreakerAtivo_deveRetornarSemPrecoUsd() {
        // Arrange
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoExemplo));
        when(cambioService.buscarTaxaCambioUsdBrl()).thenReturn(null);

        // Act
        ProdutoResponseDTO resultado = produtoService.buscarPorId(1L);

        // Assert
        assertThat(resultado.getPrecoBrl()).isEqualByComparingTo("450.00");
        assertThat(resultado.getPrecoUsd()).isNull();
        assertThat(resultado.getTaxaCambio()).isNull();
    }

    @Test
    @DisplayName("buscarPorId() deve lançar ProdutoNotFoundException quando produto não existe")
    void buscarPorId_produtoInexistente_deveLancarException() {
        // Arrange
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ProdutoNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("criar() deve salvar e retornar produto quando dados são válidos")
    void criar_dadosValidos_deveSalvarERretornar() {
        // Arrange
        ProdutoRequestDTO dto = ProdutoRequestDTO.builder()
                .nome("Mouse Gamer")
                .sku("MOU-001")
                .precoBrl(new BigDecimal("200.00"))
                .build();

        Produto salvo = Produto.builder()
                .id(2L)
                .nome(dto.getNome())
                .sku(dto.getSku())
                .precoBrl(dto.getPrecoBrl())
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        when(produtoRepository.existsBySku("MOU-001")).thenReturn(false);
        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);

        // Act
        ProdutoResponseDTO resultado = produtoService.criar(dto);

        // Assert
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNome()).isEqualTo("Mouse Gamer");
        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    @DisplayName("criar() deve lançar SkuDuplicadoException quando SKU já existe")
    void criar_skuDuplicado_deveLancarException() {
        // Arrange
        ProdutoRequestDTO dto = ProdutoRequestDTO.builder()
                .nome("Produto Duplicado")
                .sku("TEC-001")
                .precoBrl(BigDecimal.TEN)
                .build();

        when(produtoRepository.existsBySku("TEC-001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> produtoService.criar(dto))
                .isInstanceOf(SkuDuplicadoException.class)
                .hasMessageContaining("TEC-001");

        verify(produtoRepository, never()).save(any());
    }

    @Test
    @DisplayName("deletar() deve remover produto existente")
    void deletar_produtoExistente_deveRemover() {
        // Arrange
        when(produtoRepository.existsById(1L)).thenReturn(true);

        // Act
        produtoService.deletar(1L);

        // Assert
        verify(produtoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deletar() deve lançar exception para produto inexistente")
    void deletar_produtoInexistente_deveLancarException() {
        // Arrange
        when(produtoRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> produtoService.deletar(99L))
                .isInstanceOf(ProdutoNotFoundException.class);

        verify(produtoRepository, never()).deleteById(any());
    }
}
