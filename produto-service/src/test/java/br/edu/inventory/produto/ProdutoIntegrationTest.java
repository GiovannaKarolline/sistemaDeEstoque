package br.edu.inventory.produto;

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

/**
 * Testes de integração do Produto Service.
 *
 * <p>Usa @SpringBootTest para carregar o contexto completo com H2 real.
 * WireMock mocka a AwesomeAPI sem Docker e sem acesso à internet.
 *
 * <p>Princípio Susan Fowler (Testabilidade): os testes de integração
 * validam o comportamento fim-a-fim do serviço, incluindo banco de dados
 * e integração com API externa.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // Mock API
        "awesome-api.base-url=http://localhost:8099"
})
@DisplayName("ProdutoService — Testes de Integração (H2 + WireMock)")
class ProdutoIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8099));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8099);
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
    @DisplayName("POST /produtos + GET /produtos/{id} — fluxo completo com WireMock da AwesomeAPI")
    void fluxoCompletoCriacaoEConsulta() throws Exception {
        // Mock cambio
        stubFor(get(urlEqualTo("/json/last/USD-BRL"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "USDBRL": {
                                    "code": "USD",
                                    "codein": "BRL",
                                    "name": "Dólar Americano/Real Brasileiro",
                                    "bid": "5.25",
                                    "ask": "5.26",
                                    "high": "5.30",
                                    "low": "5.20"
                                  }
                                }
                                """)));

        // Cria post
        String produtoJson = """
                {
                    "nome": "Notebook Gamer",
                    "descricao": "Notebook para jogos de alta performance",
                    "sku": "NOT-001",
                    "precoBrl": 5250.00
                }
                """;

        String respCriacao = mockMvc.perform(MockMvcRequestBuilders.post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("NOT-001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Pega id
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(respCriacao).get("id").asLong();

        // Busca com USD
        mockMvc.perform(MockMvcRequestBuilders.get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBrl").value(5250.00))
                .andExpect(jsonPath("$.taxaCambio").value(5.25))
                .andExpect(jsonPath("$.precoUsd").value(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("GET /produtos/{id} deve retornar precoUsd=null quando AwesomeAPI retorna erro")
    void buscarPorId_apiExternaIndisponivel_precoUsdDeveSerNulo() throws Exception {
        // Cria primeiro
        String produtoJson = """
                { "nome": "Cadeira Gamer", "sku": "CAD-001", "precoBrl": 1200.00 }
                """;

        String respCriacao = mockMvc.perform(MockMvcRequestBuilders.post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(respCriacao).get("id").asLong();

        // Erro 503
        stubFor(get(urlEqualTo("/json/last/USD-BRL"))
                .willReturn(aResponse().withStatus(503)));

        // Fallback nulo
        mockMvc.perform(MockMvcRequestBuilders.get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBrl").value(1200.00))
                .andExpect(jsonPath("$.precoUsd").isEmpty());
    }
}
