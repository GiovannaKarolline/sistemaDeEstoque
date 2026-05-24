Documentação do Sistema de Microsserviços para Estoque

Trabalho Acadêmico — Arquitetura de Microsserviços em Java
Desenvolvido com base nos princípios teóricos e práticos de Martin Fowler (Microservices, 2014) e Susan Fowler (Production Microservices, 2016).

Arquitetura
O sistema possui um API Gateway atuando como ponto único de entrada na porta 8080. Ele recebe e roteia requisições HTTP REST de forma transparente para três microsserviços principais que compõem o sistema de retaguarda:
Produto Service (porta 8081): Gerencia os itens no catálogo, com seu banco de dados H2 próprio e integrações diretas de conversão monetária consumindo a AwesomeAPI.
Estoque Service (porta 8082): Gerencia o saldo e as movimentações, com banco de dados H2 próprio. Ele interage de forma síncrona com o Produto Service via Feign, e além disso embute um broker de mensageria ActiveMQ (porta TCP 61616) para emissão de eventos assíncronos.
Notificação Service (porta 8083): Sistema reativo que consome mensagens de log e alertas originadas pelo Estoque Service através do broker de mensageria.

Teorias de Fowler Aplicadas

Princípios de Martin Fowler — Microservices (2014)
Componentização via serviços: Todo o ecossistema é formado por 4 microsserviços independentes (Gateway, Produto, Estoque, Notificacao), e cada um é executado em seu próprio processo sem compartilhamento de memória física.
Organização baseada em capacidades de negócio: Não há camadas monolíticas. Em vez disso, os times e domínios de negócio são seccionados claramente em áreas independentes de Produto, Controle de Estoque, Sistema de Alertas e Gateway.
Smart Endpoints e Dumb Pipes: Toda a regra de validação complexa reside unicamente dentro dos controladores (endpoints) dos microsserviços. O broker ActiveMQ é apenas um condutor e não possui nenhuma lógica de transformação.
Bancos de dados descentralizados: O modelo de banco de dados não é unificado. Cada serviço cria e administra seu próprio esquema e banco de dados H2 isolado. O Estoque não acessa a tabela de Produtos diretamente.
Desenho orientado a falhas: Prevenindo paralisações em rede, implementou-se o padrão avançado de Circuit Breaker com o pacote Resilience4j no Estoque Service durante as consultas de catálogo e também no Produto Service durante integrações em dólar.
Implantação livre de amarras: O acoplamento de código é nulo. Cada serviço tem sua própria estrutura de pastas e arquivo de construção pom.xml que é compilado de forma independente dos demais.

Princípios de Susan Fowler — Production Microservices (2016)
Estabilidade garantida: Health checks constantes monitoram ativamente a saúde das aplicações por meio da rota nativa /actuator/health acoplada em todos os serviços.
Confiabilidade atestada: Circuitos de proteção (Circuit Breaker) suportados por métodos estratégicos de "Fallback" garantem confiabilidade caso dependências internas de rede entrem em instabilidade ou morram.
Escalabilidade e Ausência de Estado: Os serviços retêm os mínimos níveis de sessão, modelados em arquitetura stateless e projetados do início ao fim para suportarem escala horizontal transparente.
Níveis rigorosos de Testabilidade: O sistema assegura seu funcionamento com a união de testes unitários rápidos isolados (JUnit 5 com Mockito) e robustos testes de integração contínua isolando chamadas de terceiros (MockMvc combinado com WireMock).
Documentação intrínseca: Os engenheiros ou usuários da plataforma são abastecidos via geração de manuais baseados nos métodos Java, disponíveis online via especificação OpenAPI 3 gerada pelo Swagger UI em tempo real.
Camadas extensas de Monitoramento: Para detecção e resolução ágil de engasgos e falhas, a biblioteca Spring Boot Actuator expõe dados em todos os serviços simultaneamente.

Stack Tecnológica Utilizada
Linguagem de programação: Java 17
Framework central da aplicação: Spring Boot 3.2.5
Camada de borda e roteamento: Spring Cloud Gateway
Estratégia de comunicação síncrona interna: OpenFeign
Estratégia de comunicação assíncrona: Apache ActiveMQ Embedded acionado via protocolo JMS
Armazenamento local persistente: H2 in-memory (embedded e auto-contido para não requerer instalação externa de banco de dados)
Mapeamento e controle Objeto-Relacional (ORM): Spring Data e JPA com a tecnologia base Hibernate
Segurança e Tolerância a Falhas: Framework Resilience4j englobando o Circuit Breaker
APIs e Serviços Terceirizados consumidos: AwesomeAPI (disponível publicamente na internet para recuperar valores BRL e transacionar em USD)
Bibliotecas voltadas para Testes Automáticos: JUnit 5, Mockito, MockMvc nativo do Spring e WireMock para mock de rede
Motor de Documentação técnica de APIs: SpringDoc de formato OpenAPI 3
Ferramentas de Health Check e Métricas em tempo de execução: Spring Boot Actuator
Gestão de dependências e construtor: Maven e sua arquitetura focada em módulos.

Como Executar (sem a necessidade de utilizar o Docker)
Pré-requisitos mínimos no sistema: Deve-se possuir a linguagem Java na versão mínima 17 ou superior instalada, bem como o ecossistema Maven 3.9 ou superior ativado nas variáveis de ambiente globais. Conexão ininterrupta com a internet é necessária durante a execução devido às integrações da API externa do Dólar.

Passo 1 — Construir a arquitetura completa e baixar pacotes: Na sua interface de terminal de preferência, navegue diretamente para a pasta principal inventory-microservices e dê entrada no comando "mvn clean install -DskipTests". Aguarde até a verificação verde ser exibida.
Passo 2 — Elevar e Iniciar cada um dos serviços em paralelo: É requerido que se abra um terminal adicional distinto (sendo no total quatro) para executar a plataforma.
No terminal do Produto Service, acesse e rode: "mvn spring-boot:run -pl produto-service".
No terminal do Estoque Service (que também levanta o servidor interno ActiveMQ na porta 61616), acesse e rode: "mvn spring-boot:run -pl estoque-service".
No terminal do Notificação Service, acesse e rode: "mvn spring-boot:run -pl notificacao-service".
Por fim, no terminal principal onde o cliente irá se comunicar via porta 8080 (API Gateway), rode: "mvn spring-boot:run -pl api-gateway".

Passo 3 — Validação de funcionamento online (Swagger UI): Ao garantir a elevação de todos os sistemas locais, vá até um navegador e teste a plataforma.
Para testar como se fosse um cliente pelo Gateway unificado, acesse http://localhost:8080.
Para testar os serviços e vislumbrar documentações individualizadas em caso de falha de rota: O Produto Service fica no endereço http://localhost:8081/swagger-ui.html. O Estoque Service está listado em http://localhost:8082/swagger-ui.html. A Notificação e eventos estão listados em http://localhost:8083/swagger-ui.html.

Passo 4 — Validação minuciosa de testes automatizados: Com a garantia de execução, você poderá voltar a um terminal raiz livre na pasta do projeto e rodar o conjunto de validações utilizando o argumento "mvn test". O terminal imprimirá detalhadamente o sucesso de cada classe testada.

Endpoints Principais
Fluxo de Produto Service (rotas de tráfego repassado via Gateway pelo prefixo /produtos/**): É o centro onde residem métodos de criação, leitura, atualização e exclusão plena (verbos POST, GET, PUT, e DELETE). A principal diferença acontece no método individual onde a ID é enviada para realizar leitura, resultando em dados transformados via AwesomeAPI de BRL a USD.
Fluxo de Estoque Service (rotas de tráfego repassado via Gateway pelo prefixo /estoque/**): Possui a missão primordial de gerenciar saldo numérico na base local. Realiza transações recebidas unicamente através de POST, listagem local de movimentações passadas efetuadas e requisição individualizada para consultar o saldo pontual através da ID do Produto originário no catálogo base.
Fluxo de Notificação Service (rotas de tráfego repassado via Gateway pelo prefixo /notificacoes/**): Sua função reativa reside unicamente na exposição de dados captados via eventos no barramento JMS e salvos localmente, gerando a visualização passiva e a distribuição pontual das anomalias lidas da fila sob o método GET.

Fluxo Assíncrono de Mensageria (Mecânica e Reações do ActiveMQ)
No momento em que há uma solicitação de POST confirmando a diminuição de número de produtos sob movimentação via Estoque Service, uma verificação inteligente interna cruza o limite da quantidade remanescente no estoque.
A condição lógica de alarme avalia se o saldo total numérico atual após as subtrações for abaixo de exatas dez unidades de estoque. Caso este marco crítico seja alcançado, o Serviço de Estoque toma uma atitude proativa isolada e atira a emissão de notificação formatada para o servidor principal dentro da fila chamada estoque.alerta.
No outro extremo do espectro, isolado totalmente sem comunicação HTTP, o Notificação Service acorda como um vigia consumindo as informações atiradas passivamente através do JmsListener e persiste imediatamente a string para leitura pública de avisos urgentes dos produtos em falha.

Matriz de Cobertura de Testes
O projeto possui defesas bem definidas para suas lógicas fundamentais englobando testes individuais unitários sobre os Services (Produto, Estoque, Notificação), garantindo isolamento da lógica bruta de negócios amparado no Mockito.
As amarrações da web e fluxos das controladoras foram validadas efetuando simulações de Slice de Spring via WebMvcTest com a poderosa estrutura MockMvc.
Ao lado da camada superficial, os cenários completos do tipo "end-to-end" integram efetivamente componentes simulando o sistema do banco de dados (H2 acoplado dinamicamente via profile em classes do tipo SpringBootTest) junto da estrutura simulada autônoma de ligações do lado de fora englobada e administrada por interceptadores web do WireMock de modo a bloquear toda interação do cliente com instabilidades oriundas da AwesomeAPI e simular tráfego HTTP com segurança.
