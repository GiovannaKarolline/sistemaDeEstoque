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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        // Redireciona chamadas da AwesomeAPI para o WireMock local
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
        // 1. Mockar AwesomeAPI para retornar cotação USD-BRL = 5.25
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

        // 2. Criar produto via POST
        String produtoJson = """
                {
                    "nome": "Notebook Gamer",
                    "descricao": "Notebook para jogos de alta performance",
                    "sku": "NOT-001",
                    "precoBrl": 5250.00
                }
                """;

        String respCriacao = mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("NOT-001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrair ID criado
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(respCriacao).get("id").asLong();

        // 3. Buscar produto por ID — deve incluir preço em USD (5250 / 5.25 = 1000)
        mockMvc.perform(get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBrl").value(5250.00))
                .andExpect(jsonPath("$.taxaCambio").value(5.25))
                .andExpect(jsonPath("$.precoUsd").value(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("GET /produtos/{id} deve retornar precoUsd=null quando AwesomeAPI retorna erro")
    void buscarPorId_apiExternaIndisponivel_precoUsdDeveSerNulo() throws Exception {
        // 1. Criar produto primeiro
        String produtoJson = """
                { "nome": "Cadeira Gamer", "sku": "CAD-001", "precoBrl": 1200.00 }
                """;

        String respCriacao = mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(respCriacao).get("id").asLong();

        // 2. Simular AwesomeAPI com erro 503
        stubFor(get(urlEqualTo("/json/last/USD-BRL"))
                .willReturn(aResponse().withStatus(503)));

        // 3. Buscar produto — precoUsd deve ser null (circuit breaker / fallback)
        mockMvc.perform(get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoBrl").value(1200.00))
                .andExpect(jsonPath("$.precoUsd").isEmpty());
    }
}
