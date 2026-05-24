# Produto Service

## 1. Descrição Funcional
- **Nome do microsserviço:** Produto Service
- **Objetivo e responsabilidades principais:** O serviço atua como o catálogo central de itens do sistema. Sua responsabilidade principal é fornecer operações CRUD para a gestão de produtos, garantindo as regras de negócio associadas aos cadastros. Além disso, ele atua ativamente enriquecendo os dados retornados ao usuário, consultando a cotação do dólar em tempo real para apresentar os preços em moedas internacionais (USD).

## 2. Endpoints da API
Abaixo estão os endpoints expostos, documentados internamente via OpenAPI/Swagger:

- **`GET /produtos`**
  - **Descrição:** Lista de forma paginada/simplificada todos os produtos cadastrados no sistema. (Por otimização de performance, não aplica a conversão cambial na listagem global).
- **`GET /produtos/{id}`**
  - **Descrição:** Busca e retorna os dados detalhados de um produto específico pelo seu ID. Internamente, realiza a requisição à API de câmbio para incluir o valor convertido para Dólar (USD).
- **`POST /produtos`**
  - **Descrição:** Cria um novo produto no banco de dados. Exige validação de campos obrigatórios e unicidade do SKU.
- **`PUT /produtos/{id}`**
  - **Descrição:** Atualiza as informações (como nome, descrição e preço BRL) de um produto existente.
- **`DELETE /produtos/{id}`**
  - **Descrição:** Remove um produto fisicamente do catálogo de estoque.

## 3. Exemplo de Requisição e Resposta

**Exemplo de criação de produto (`POST /produtos`):**

*Requisição (JSON):*
```json
{
  "nome": "Notebook Gamer",
  "descricao": "Notebook para jogos de alta performance",
  "sku": "NOT-001",
  "precoBrl": 5250.00
}
```

*Resposta (`201 Created`):*
```json
{
  "id": 2,
  "nome": "Notebook Gamer",
  "descricao": "Notebook para jogos de alta performance",
  "sku": "NOT-001",
  "precoBrl": 5250.00,
  "precoUsd": 1000.00,
  "taxaCambio": 5.25,
  "criadoEm": "2026-05-23T22:43:02",
  "atualizadoEm": "2026-05-23T22:43:02"
}
```

## 4. Dependências Externas
- **Outros microsserviços consumidos:** Nenhum (o Produto Service atua na base da arquitetura e é consumido por outros, como o Estoque Service).
- **Banco de dados:** Banco de dados relacional (H2 in-memory) embarcado, manipulado via Spring Data JPA.
- **Fila ou broker de mensagens:** N/A para este serviço.
- **APIs externas:** Consome a `AwesomeAPI` (`economia.awesomeapi.com.br`) sem necessidade de autenticação para resgatar a última cotação do par USD-BRL.

## 5. Responsável pelo Serviço
- **Equipe:** Squad de Catálogo / Inventário.
- **Responsável:** Desenvolvedor alocado / Giovanna Karolline (Owner do Repositório).

## 6. Procedimentos Básicos de Operação
- **Como executar localmente:** No diretório do serviço, utilize o wrapper do Maven ou uma instalação local: 
  ```bash
  mvn spring-boot:run
  ```
  O serviço irá subir na porta configurada (default: 8081).
- **Como verificar logs:** Os logs da aplicação são impressos diretamente na saída padrão (console/stdout) através do SLF4J + Logback configurados pelo Spring Boot.
- **Endpoint de health check:** Por meio do Spring Boot Actuator, pode-se verificar a saúde em `http://localhost:8081/actuator/health`.
- **Documentação Interativa:** Acesse o Swagger UI em `http://localhost:8081/swagger-ui.html`.
- **Como reiniciar o serviço:** Como roda embutido (Tomcat), basta encerrar o processo atrelado no terminal (ex: `CTRL + C`) e executar o comando de start novamente.

## 7. Regras de Negócio
- **Unicidade de SKU:** Não é permitido o cadastro de dois produtos com o mesmo código `sku` (Stock Keeping Unit). O sistema deve retornar um erro HTTP 400 em caso de duplicidade.
- **Conversão de Câmbio em Tempo Real:** O preço é armazenado em Reais (BRL). Ao consultar o detalhe de um produto, o serviço consome a cotação do Dólar para computar o valor estimado.
- **Resiliência e Fallback (Circuit Breaker):** Caso a AwesomeAPI esteja inoperante ou apresentando instabilidades graves, o Circuit Breaker (Resilience4j) é ativado. Quando aberto, uma função de *fallback* é disparada preenchendo o `precoUsd` como `null`, impedindo que o `produto-service` caia devido a falhas em cascata, mantendo os endpoints vitais no ar.

## 8. Eventos Publicados ou Consumidos (se aplicável)
- *Não aplicável a este microsserviço no momento.* Toda a comunicação dele é puramente síncrona via requisições REST/HTTP.

## 9. Métricas Monitoradas
Através da integração com o Spring Boot Actuator (`/actuator/metrics`), as seguintes métricas técnicas e de resiliência são relevantes e observáveis:
- `http.server.requests`: Quantidade e tempo de resposta geral (latência) dos endpoints REST.
- `resilience4j.circuitbreaker.state`: Estado atual do circuito em relação à AwesomeAPI (CLOSED, OPEN, HALF_OPEN) para monitorar instabilidades em integrações externas.
- `hikaricp.connections`: Utilização do pool de conexões com o banco de dados.

## 10. ADR Relacionado (Decisão Arquitetural)
- **ADR-001: Isolamento de Base de Dados por Microsserviço:**
  *Decisão:* O `produto-service` é o guardião único dos dados de produto. Ele possui seu próprio banco de dados, não compartilhando tabelas com o `estoque-service` ou outros. Outros serviços que precisarem dos dados de produto devem necessariamente realizar chamadas de rede API REST para ele.
- **ADR-002: Smart Endpoints, Dumb Pipes:**
  *Decisão:* Toda a inteligência e orquestração de resiliência residem no próprio microsserviço (via Resilience4j), sem depender de um Enterprise Service Bus (ESB) complexo na rede para tratamento de falhas HTTP.
