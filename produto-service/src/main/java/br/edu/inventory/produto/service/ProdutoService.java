package br.edu.inventory.produto.service;

import br.edu.inventory.produto.dto.ProdutoRequestDTO;
import br.edu.inventory.produto.dto.ProdutoResponseDTO;
import br.edu.inventory.produto.exception.ProdutoNotFoundException;
import br.edu.inventory.produto.exception.SkuDuplicadoException;
import br.edu.inventory.produto.model.Produto;
import br.edu.inventory.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de negócio do Produto.
 *
 * <p>Princípio Martin Fowler (Smart Endpoints): toda lógica de negócio
 * reside no serviço, não no controller nem no gateway.
 *
 * <p>Princípio Susan Fowler (Testabilidade): serviço stateless e com
 * dependências injetadas, facilitando testes com Mockito.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CambioService cambioService;

    /**
     * Lista todos os produtos. Sem conversão de câmbio para otimizar performance
     * em consultas em massa (N+1 query prevention).
     */
    public List<ProdutoResponseDTO> listarTodos() {
        log.info("Listando todos os produtos");
        return produtoRepository.findAll()
                .stream()
                .map(p -> toResponseDTO(p, null, null))
                .collect(Collectors.toList());
    }

    /**
     * Busca produto por ID e inclui preço convertido para USD via API externa.
     */
    public ProdutoResponseDTO buscarPorId(Long id) {
        log.info("Buscando produto id={}", id);
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));

        // Consulta cambio
        BigDecimal taxaCambio = cambioService.buscarTaxaCambioUsdBrl();
        BigDecimal precoUsd = calcularPrecoUsd(produto.getPrecoBrl(), taxaCambio);

        return toResponseDTO(produto, precoUsd, taxaCambio);
    }

    /**
     * Cria um novo produto após validação de SKU único.
     */
    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        log.info("Criando produto com SKU={}", dto.getSku());

        if (produtoRepository.existsBySku(dto.getSku())) {
            throw new SkuDuplicadoException(dto.getSku());
        }

        Produto produto = Produto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .sku(dto.getSku())
                .precoBrl(dto.getPrecoBrl())
                .build();

        Produto salvo = produtoRepository.save(produto);
        log.info("Produto criado com id={}", salvo.getId());
        return toResponseDTO(salvo, null, null);
    }

    /**
     * Atualiza produto existente.
     */
    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        log.info("Atualizando produto id={}", id);

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));

        if (produtoRepository.existsBySkuAndIdNot(dto.getSku(), id)) {
            throw new SkuDuplicadoException(dto.getSku());
        }

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setSku(dto.getSku());
        produto.setPrecoBrl(dto.getPrecoBrl());

        Produto atualizado = produtoRepository.save(produto);
        return toResponseDTO(atualizado, null, null);
    }

    /**
     * Remove produto por ID.
     */
    @Transactional
    public void deletar(Long id) {
        log.info("Deletando produto id={}", id);
        if (!produtoRepository.existsById(id)) {
            throw new ProdutoNotFoundException(id);
        }
        produtoRepository.deleteById(id);
    }

    // Helpers

    private BigDecimal calcularPrecoUsd(BigDecimal precoBrl, BigDecimal taxaCambio) {
        if (taxaCambio == null || taxaCambio.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return precoBrl.divide(taxaCambio, 4, RoundingMode.HALF_UP);
    }

    private ProdutoResponseDTO toResponseDTO(Produto p, BigDecimal precoUsd, BigDecimal taxaCambio) {
        return ProdutoResponseDTO.builder()
                .id(p.getId())
                .nome(p.getNome())
                .descricao(p.getDescricao())
                .sku(p.getSku())
                .precoBrl(p.getPrecoBrl())
                .precoUsd(precoUsd)
                .taxaCambio(taxaCambio)
                .criadoEm(p.getCriadoEm())
                .atualizadoEm(p.getAtualizadoEm())
                .build();
    }
}
