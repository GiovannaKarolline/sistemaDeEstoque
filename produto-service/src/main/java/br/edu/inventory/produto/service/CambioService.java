package br.edu.inventory.produto.service;

import br.edu.inventory.produto.dto.AwesomeApiResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

/**
 * Serviço de consulta de câmbio via API externa (AwesomeAPI).
 *
 * <p><strong>API Externa</strong>: economia.awesomeapi.com.br — pública, sem autenticação.
 * Retorna a cotação do par USD-BRL.
 *
 * <p>Princípio Susan Fowler (Confiabilidade): utiliza Circuit Breaker (Resilience4j)
 * para evitar falha em cascata caso a API externa fique indisponível.
 * Quando o circuito está aberto, retorna um valor de fallback (null),
 * e o serviço continua funcionando sem o preço em USD.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CambioService {

    private static final String CIRCUIT_BREAKER_NAME = "awesomeApi";
    private static final String AWESOME_API_URL =
            "https://economia.awesomeapi.com.br/json/last/USD-BRL";

    private final WebClient webClient;

    /**
     * Busca a cotação atual de USD em relação ao BRL.
     *
     * @return taxa de câmbio (BRL por 1 USD), ou null em caso de falha
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "cotacaoFallback")
    public BigDecimal buscarTaxaCambioUsdBrl() {
        log.info("Consultando cotação USD-BRL na AwesomeAPI...");

        AwesomeApiResponseDTO response = webClient
                .get()
                .uri(AWESOME_API_URL)
                .retrieve()
                .bodyToMono(AwesomeApiResponseDTO.class)
                .block();

        if (response == null || response.getUsdbrl() == null) {
            log.warn("AwesomeAPI retornou resposta vazia");
            return null;
        }

        BigDecimal taxa = response.getUsdbrl().getBid();
        log.info("Taxa de câmbio USD-BRL obtida: {}", taxa);
        return taxa;
    }

    /**
     * Fallback do Circuit Breaker: retorna null indicando que a taxa
     * não está disponível no momento.
     *
     * <p>O serviço de produto continua funcionando; apenas o preço em USD
     * ficará ausente na resposta (null).
     */
    public BigDecimal cotacaoFallback(Exception ex) {
        log.error("Circuit Breaker ativo — AwesomeAPI indisponível: {}", ex.getMessage());
        return null;
    }
}
