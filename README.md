# 📦 Sistema de Microsserviços para Estoque

> Trabalho Acadêmico — Arquitetura de Microsserviços em Java  
> Baseado nos princípios de **Martin Fowler** (*Microservices*, 2014) e **Susan Fowler** (*Production Microservices*, 2016)

---

## 🏛️ Arquitetura

```
Cliente HTTP
     │
     ▼
┌─────────────────────────┐
│    API Gateway  :8080   │   ← Ponto único de entrada
└────────────┬────────────┘
             │ HTTP/REST
    ┌─────────┼──────────┐
    ▼         ▼          ▼
┌────────┐ ┌────────┐ ┌─────────────┐
│Produto │ │Estoque │ │ Notificação │
│  :8081 │ │  :8082 │ │    :8083    │
└───┬────┘ └───┬────┘ └──────┬──────┘
    │H2        │H2      consume│
    │    Feign │        JMS   │
    │◄─────────┘              │
    │         ActiveMQ (TCP :61616, embedded no Estoque Service)
    │                         ▲
    └─────────────────────────┘
```

## 🎓 Teorias de Fowler Aplicadas

### Martin Fowler — Microservices (2014)
| Princípio | Como é aplicado |
|---|---|
| Componentização via serviços | 4 serviços independentes, cada um em seu próprio processo |
| Organizado por capacidade de negócio | Produto, Estoque, Notificação, Gateway |
| Smart Endpoints, Dumb Pipes | Lógica nos serviços; broker apenas transporta mensagens |
| Banco de dados descentralizado | Cada serviço tem seu próprio H2 isolado |
| Design para falha | Circuit Breaker (Resilience4j) no Estoque ao chamar Produto |
| Implantação independente | Cada serviço tem seu próprio `pom.xml` e pode ser iniciado separadamente |

### Susan Fowler — Production Microservices (2016)
| Princípio | Como é aplicado |
|---|---|
| Estabilidade | Health checks via `/actuator/health` em todos os serviços |
| Confiabilidade | Circuit Breaker + Fallback no Estoque Service |
| Escalabilidade | Serviços stateless, prontos para escala horizontal |
| Testabilidade | Testes unitários (JUnit 5 + Mockito) e integração (MockMvc + WireMock) |
| Documentação | OpenAPI 3 / Swagger UI em todos os serviços HTTP |
| Monitoramento | Spring Boot Actuator em todos os serviços |

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Gateway | Spring Cloud Gateway |
| Comunicação síncrona | OpenFeign (Estoque → Produto) |
| Comunicação assíncrona | Apache ActiveMQ Embedded (JMS) |
| Banco de dados | H2 in-memory (embedded, sem instalação) |
| ORM | Spring Data JPA + Hibernate |
| Resiliência | Resilience4j (Circuit Breaker) |
| API Externa | AwesomeAPI (cotação BRL→USD, pública) |
| Testes | JUnit 5 + Mockito + MockMvc + WireMock |
| Documentação | SpringDoc OpenAPI 3 |
| Monitoramento | Spring Boot Actuator |
| Build | Maven (multi-module) |

---

## 🚀 Como Executar (sem Docker)

### Pré-requisitos
- Java 17+
- Maven 3.9+
- Conexão com internet (para AwesomeAPI)

### Passo 1 — Compilar todos os módulos
```bash
cd inventory-microservices
mvn clean install -DskipTests
```

### Passo 2 — Iniciar os serviços (em terminais separados)

**Terminal 1 — Produto Service:**
```bash
mvn spring-boot:run -pl produto-service
```

**Terminal 2 — Estoque Service** *(também inicia o broker ActiveMQ na porta 61616)*:
```bash
mvn spring-boot:run -pl estoque-service
```

**Terminal 3 — Notificação Service:**
```bash
mvn spring-boot:run -pl notificacao-service
```

**Terminal 4 — API Gateway:**
```bash
mvn spring-boot:run -pl api-gateway
```

### Passo 3 — Testar via Swagger UI
| Serviço | Swagger UI |
|---|---|
| API Gateway (todos os serviços) | http://localhost:8080 |
| Produto Service | http://localhost:8081/swagger-ui.html |
| Estoque Service | http://localhost:8082/swagger-ui.html |
| Notificação Service | http://localhost:8083/swagger-ui.html |

### Passo 4 — Rodar testes automatizados
```bash
mvn test
```

---

## 📡 Endpoints Principais

### Produto Service (via Gateway: `/produtos/**`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/produtos` | Listar todos os produtos |
| `GET` | `/produtos/{id}` | Buscar produto por ID (inclui preço em USD via AwesomeAPI) |
| `POST` | `/produtos` | Criar produto |
| `PUT` | `/produtos/{id}` | Atualizar produto |
| `DELETE` | `/produtos/{id}` | Remover produto |

### Estoque Service (via Gateway: `/estoque/**`)
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/estoque/movimentacoes` | Registrar entrada ou saída |
| `GET` | `/estoque/movimentacoes` | Listar movimentações |
| `GET` | `/estoque/saldo/{produtoId}` | Consultar saldo atual |

### Notificação Service (via Gateway: `/notificacoes/**`)
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/notificacoes/alertas` | Listar alertas de estoque baixo |
| `GET` | `/notificacoes/alertas/{produtoId}` | Alertas por produto |

---

## 📬 Fluxo de Mensageria (ActiveMQ)

1. `POST /estoque/movimentacoes` registra uma movimentação
2. Se saldo total < 10 unidades → Estoque Service publica evento na fila `estoque.alerta`
3. Notificação Service consome o evento via `@JmsListener` e persiste um `Alerta`

---

## 🧪 Cobertura de Testes

| Teste | Tipo | Ferramentas |
|---|---|---|
| `ProdutoServiceTest` | Unitário | JUnit 5 + Mockito |
| `ProdutoControllerTest` | Fatia (Slice) | `@WebMvcTest` + MockMvc |
| `ProdutoIntegrationTest` | Integração | `@SpringBootTest` + H2 + WireMock |
| `EstoqueServiceTest` | Unitário | JUnit 5 + Mockito |
| `EstoqueControllerTest` | Fatia (Slice) | `@WebMvcTest` + MockMvc |
| `EstoqueEventConsumerTest` | Unitário | JUnit 5 + Mockito |
| `AlertaServiceTest` | Unitário | JUnit 5 + Mockito |
