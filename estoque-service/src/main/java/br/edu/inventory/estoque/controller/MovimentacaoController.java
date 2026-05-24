package br.edu.inventory.estoque.controller;

import br.edu.inventory.estoque.dto.MovimentacaoRequestDTO;
import br.edu.inventory.estoque.dto.MovimentacaoResponseDTO;
import br.edu.inventory.estoque.dto.SaldoResponseDTO;
import br.edu.inventory.estoque.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Operações de movimentação e saldo de estoque")
public class MovimentacaoController {

    private final EstoqueService estoqueService;

    @PostMapping("/movimentacoes")
    @Operation(summary = "Registrar movimentação (ENTRADA ou SAIDA)")
    @ApiResponse(responseCode = "201", description = "Movimentação registrada com sucesso")
    @ApiResponse(responseCode = "400", description = "Estoque insuficiente para saída")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<MovimentacaoResponseDTO> registrar(
            @Valid @RequestBody MovimentacaoRequestDTO dto) {
        MovimentacaoResponseDTO salva = estoqueService.registrarMovimentacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/movimentacoes/{produtoId}")
    @Operation(summary = "Listar histórico de movimentações de um produto")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listar(@PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.listarMovimentacoes(produtoId));
    }

    @GetMapping("/saldo/{produtoId}")
    @Operation(summary = "Consultar saldo atual de um produto")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    public ResponseEntity<SaldoResponseDTO> consultarSaldo(@PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.consultarSaldo(produtoId));
    }
}
