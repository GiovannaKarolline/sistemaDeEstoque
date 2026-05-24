package br.edu.inventory.produto.controller;

import br.edu.inventory.produto.dto.ProdutoRequestDTO;
import br.edu.inventory.produto.dto.ProdutoResponseDTO;
import br.edu.inventory.produto.exception.GlobalExceptionHandler;
import br.edu.inventory.produto.exception.ProdutoNotFoundException;
import br.edu.inventory.produto.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de fatia (Slice Tests) do ProdutoController.
 *
 * <p>Usa @WebMvcTest para inicializar apenas a camada web,
 * sem carregar o contexto completo — mais rápido e focado.
 *
 * <p>Princípio Susan Fowler (Testabilidade): testes de camada
 * isolados garantem que o contrato HTTP está correto.
 */
@WebMvcTest(ProdutoController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ProdutoController — Testes de Fatia HTTP")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoService produtoService;

    private ProdutoResponseDTO responseExemplo() {
        return ProdutoResponseDTO.builder()
                .id(1L)
                .nome("Teclado Mecânico")
                .sku("TEC-001")
                .precoBrl(new BigDecimal("450.00"))
                .precoUsd(new BigDecimal("90.00"))
                .taxaCambio(new BigDecimal("5.00"))
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /produtos deve retornar 200 com lista de produtos")
    void listarTodos_deveRetornar200() throws Exception {
        when(produtoService.listarTodos()).thenReturn(List.of(responseExemplo()));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].nome").value("Teclado Mecânico"))
                .andExpect(jsonPath("$[0].sku").value("TEC-001"));
    }

    @Test
    @DisplayName("GET /produtos/{id} deve retornar 200 com preço em USD")
    void buscarPorId_deveRetornar200ComPrecoUsd() throws Exception {
        when(produtoService.buscarPorId(1L)).thenReturn(responseExemplo());

        mockMvc.perform(get("/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.precoBrl").value(450.00))
                .andExpect(jsonPath("$.precoUsd").value(90.00))
                .andExpect(jsonPath("$.taxaCambio").value(5.00));
    }

    @Test
    @DisplayName("GET /produtos/{id} deve retornar 404 quando produto não existe")
    void buscarPorId_inexistente_deveRetornar404() throws Exception {
        when(produtoService.buscarPorId(99L))
                .thenThrow(new ProdutoNotFoundException(99L));

        mockMvc.perform(get("/produtos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("POST /produtos deve retornar 201 quando dados são válidos")
    void criar_dadosValidos_deveRetornar201() throws Exception {
        ProdutoRequestDTO request = ProdutoRequestDTO.builder()
                .nome("Mouse Gamer")
                .sku("MOU-001")
                .precoBrl(new BigDecimal("200.00"))
                .build();

        ProdutoResponseDTO resposta = ProdutoResponseDTO.builder()
                .id(2L)
                .nome("Mouse Gamer")
                .sku("MOU-001")
                .precoBrl(new BigDecimal("200.00"))
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();

        when(produtoService.criar(any(ProdutoRequestDTO.class))).thenReturn(resposta);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Mouse Gamer"));
    }

    @Test
    @DisplayName("POST /produtos deve retornar 400 quando nome está ausente")
    void criar_semNome_deveRetornar400() throws Exception {
        ProdutoRequestDTO request = ProdutoRequestDTO.builder()
                .sku("MOU-001")
                .precoBrl(new BigDecimal("200.00"))
                .build(); // Sem nome

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.nome").exists());
    }

    @Test
    @DisplayName("DELETE /produtos/{id} deve retornar 204 quando produto existe")
    void deletar_existente_deveRetornar204() throws Exception {
        doNothing().when(produtoService).deletar(1L);

        mockMvc.perform(delete("/produtos/1"))
                .andExpect(status().isNoContent());
    }
}
