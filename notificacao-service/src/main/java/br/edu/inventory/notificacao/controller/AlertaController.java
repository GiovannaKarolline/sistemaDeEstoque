package br.edu.inventory.notificacao.controller;

import br.edu.inventory.notificacao.model.Alerta;
import br.edu.inventory.notificacao.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Consulta de alertas gerados por eventos do sistema")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping("/alertas")
    @Operation(summary = "Listar todos os alertas", description = "Retorna todos os alertas de estoque baixo.")
    public ResponseEntity<List<Alerta>> listarTodos() {
        return ResponseEntity.ok(alertaService.listarTodos());
    }

    @GetMapping("/alertas/{produtoId}")
    @Operation(summary = "Listar alertas por produto")
    public ResponseEntity<List<Alerta>> listarPorProduto(@PathVariable Long produtoId) {
        return ResponseEntity.ok(alertaService.listarPorProduto(produtoId));
    }
}
