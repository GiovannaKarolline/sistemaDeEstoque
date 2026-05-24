package br.edu.inventory.estoque;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Redireciona chamadas do Feign para o ProdutoService mockado (WireMock)
        "produto-service.url=http://localhost:8098",
        "spring.artemis.embedded.enabled=true"
})
@DisplayName("EstoqueService — Testes de Integração (H2 + WireMock + Artemis)")
class EstoqueIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8098));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8098);
    }

    @AfterAll
    static void pararWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetarWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("POST /estoque/movimentacoes deve verificar Produto via Feign e registrar entrada")
    void registrarEntrada_comProdutoExistente_deveRetornar201() throws Exception {
        // Mock do Produto Service retornando 200 OK (Produto Existe)
        stubFor(get(urlEqualTo("/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "nome": "Produto de Teste",
                                  "sku": "TST-001"
                                }
                                """)));

        String requestJson = """
                {
                    "produtoId": 1,
                    "tipo": "ENTRADA",
                    "quantidade": 50
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/estoque/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(50));
    }

    @Test
    @DisplayName("POST /estoque/movimentacoes deve retornar 404 se Produto Service retornar 404")
    void registrarMovimentacao_produtoInexistente_deveRetornar404() throws Exception {
        // Mock do Produto Service retornando 404
        stubFor(get(urlEqualTo("/99"))
                .willReturn(aResponse().withStatus(404)));

        String requestJson = """
                {
                    "produtoId": 99,
                    "tipo": "ENTRADA",
                    "quantidade": 10
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/estoque/movimentacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado no Produto Service"));
    }
}
