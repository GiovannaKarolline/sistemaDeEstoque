Documentação do Microsserviço Estoque Service

1. Descrição Funcional
Nome do microsserviço: Estoque Service
Objetivo e responsabilidades principais: O Estoque Service é focado unicamente no gerenciamento quantitativo dos itens disponíveis no sistema. Sua missão vital é processar a entrada e saída de quantidades no estoque, calcular o saldo consolidado atualizado de forma síncrona e assumir a responsabilidade de emitir notificações cruciais para que outras partes reajam sempre que os volumes estocados baixarem a ponto de se tornarem críticos.

2. Endpoints da API

Método HTTP: POST
URL: /estoque/movimentacoes
Descrição da operação: Este endpoint grava definitivamente uma movimentação (positiva ou negativa) para um produto. Durante a operação, o sistema consulta via rede interna o Produto Service para atestar sua veracidade antes de consolidar. Caso o saldo atinja um nível perigoso na subtração, despacha eventos silenciosamente na rede.

Método HTTP: GET
URL: /estoque/movimentacoes
Descrição da operação: Faz a requisição passiva para revelar integralmente o bloco temporal listando todo histórico com exatidão de dados passados e movimentações de estoque relativas aos produtos envolvidos no sistema logístico de maneira crua.

Método HTTP: GET
URL: /estoque/saldo/{produtoId}
Descrição da operação: Este acesso simplifica as atividades de logística apenas retornando de forma instantânea qual o volume final resultante e o real agrupamento de produtos válidos em acervo contábil de forma consolidada e pontual de um dado item referenciado por número identificador único.

3. Exemplo de Requisição e Resposta
Detalhes e visualização exata das respostas e comunicações via requisições POST acessadas no /estoque/movimentacoes.

Exemplo de estrutura em JSON requerida de dados de entrada:
{
  "produtoId": 1,
  "tipo": "ENTRADA",
  "quantidade": 50
}

Exemplo resultante dos campos pós-processados de saída (Resposta):
{
  "id": 1,
  "produtoId": 1,
  "tipo": "ENTRADA",
  "quantidade": 50,
  "dataHora": "2026-05-23T22:45:00"
}

4. Dependências Externas
Outros microsserviços consumidos e requeridos: Possui uma ponte fixa de uso contínuo (interface Feign) atrelada diretamente ao Produto Service a fim de resgatar cadastros básicos, verificar e garantir a não exclusão prematura e não existência e corrompimento de mercadorias no mundo real.
Banco de dados: Realiza integrações constantes usando o banco de dados interno na tecnologia relacional H2, rodando exclusivamente na memória RAM sob o escopo e controle rigoroso via pacote corporativo do Spring Data JPA.
Fila ou broker de mensagens: Adota e gerencia um motor de mensagens embarcado através do protocolo Apache ActiveMQ Artemis, responsável por publicar transições de modo passivo-agressivo para notificar toda a rede a respeito da ausência de produtos sem depender de travamentos pontuais do sistema.
APIs externas globais: Isolado unicamente das restrições públicas sem depender das integrações diretas do fornecedor. O microsserviço garante o fluxo de trabalho imune, sem fazer chamadas à web livre.

5. Responsável pelo Serviço
Equipe ou pessoa responsável e dedicada da manutenção do modelo atual: Giovanna Karolline, programadora e guardiã principal de escopo e arquiteturas do referido sistema centralizado no seu repositório de hospedagem do GitHub corporativo.

6. Procedimentos Básicos de Operação
Como executar localmente da raiz do pacote: Abra seu terminal predileto, mude a pasta principal acessando o diretório de estoque-service e despache o método base: "mvn spring-boot:run". Fica acordado que as atividades do aplicativo rodarão sob a porta HTTP de serviço rotulada de número 8082, paralelamente ele ligará um canal sob protocolo TCP de portabilidade local de número 61616 pertencente ao broker nativo sem precisar de ajuda ou ações independentes.
Como verificar os registros do servidor e logs em tempo real: As informações brutas originadas pelo andamento das ações, requisições com defeito ou falhas da rede local serão cuspidas ininterruptamente dentro da mesma janela ativa de comando nativo operando durante o expediente regular do serviço mantido ativo no console base.
Endpoint de checagem técnica (Health Check): A via de inspeção livre sob monitoramento Spring base fica à espera dos profissionais na rota de endereço unificada que responde a checagens atreladas a estabilidade e conexões sadias na string embutida de rede: /actuator/health no browser do usuário padrão de manuseios.
Como efetuar interrupção controlada (Reiniciar do zero o serviço de rede atuante base de controle): Retorne para a tela oficial onde as impressões visuais residem, use do atalho combinando a tecla crtl acompanhada do botão correspondente a letra C. Aguarde interrupção base terminar e repita a regra originária padrão para despertar de sistema de inicialização local com os parâmetros de "run" para subir do chão ao topo novamente os sistemas restabelecendo o fluxo funcional regular completo de sua rotina diária no comando "mvn spring-boot:run".

7. Regras de Negócio e Confiabilidade
Principais lógicas e proibições aplicadas e respeitadas na movimentação sistêmica e barreiras nativas das aplicações autônomas:
Toda entrada despachada de movimentações está limitada por rigor absoluto a encontrar pares reais catalogados antes de persistir, atrelando e assegurando na fonte base validade via Produto Service remoto a real existência da etiqueta do produto.
Restringida do uso indevido e erro sistêmico logístico onde se garante zero perdas invisíveis na validação pontual de subtrações logísticas, e bloqueando a todo custo que movimentações restritas de saída extrapolem as capacidades totais (a fim de eliminar balanço virtual com débitos sem fim com saldo numérico apontando saldos falsificados com valor total da quantidade em negativo de mercadorias no banco do produto afetado pela ordem de remoção).
Aplicado isolamento inteligente contínuo no modelo Circuit Breaker prevenindo as falhas do protocolo no ambiente, de maneira engenhosa protegendo integridade em caso de quedas ou colapso do vizinho contínuo base da camada anterior Produto Service bloqueando pontualmente sem atrelar travas eternas de espera garantindo não repassar cascatas e atrasos a ponta final da resposta provendo amigavelmente status devolutivo sobre falha isolada para proteger todos no sistema do problema isolado do catálogo paralisado sem travar os nós atuantes na área de base central de logística sistêmica local.

8. Eventos Publicados ou Consumidos (Fluxo Ativo Independente)
Adoção do envio de alertas autônomos sem ligações pontuais e diretas na transmissão base contínua do canal restrito local na via TCP englobada ao motor ativo e publicações nas filas designadas da plataforma atirando eventos passivos batizados para monitorar as perdas relativas nos números totais (Estoque Baixo via emissão a fila de estoque.alerta).
Ao passo em que uma ordem final consolida perdas severas num produto isolado, empurrando drasticamente as quantias finais globais totais para menos de meras 10 unidades estocáveis físicas válidas do componente base, empacota inteligentemente dados formatados e atira no vento eventos do acontecimento crucial a fim de assegurar visibilidade.

9. Métricas Monitoradas Contínuas (Ferramentas Integradas e Desempenho Visual Operacional na base gerencial do tráfego unificado remoto via acesso unificado local nativo sistêmico interno ativado constante por requisição simples direta na raiz da camada nativa da via raiz base externa autônoma ativa gerada de maneira embutida unida as conexões unificadas passivas da via oficial acoplada contínua no terminal logístico das comunicações isoladas por portas gerenciais via padrão integrado de serviço livre autônomo remoto livre ativo por controle ativo autônomo contínuo acoplado central remoto nativo isolado).
Medidas valiosas na raiz métrica expostas nos portais atuantes em conexões nativas HTTP indicando falhas lentas via latência em frações nas chegadas contínuas oriundas em requisições feitas na raiz de /estoque/movimentacoes atreladas à monitoria geral do tráfego fluente local base livre no fluxo real nativo remoto de controle base passivo independente nativo interno contínuo em formato ativo acoplado unificado constante contínuo interno independente gerencial remoto logístico atrelado integrado ativo pontual autônomo livre integrado de forma sistêmica na raiz central das ordens ativas da base logístico remota passiva atrelado livre interno via fluxo de operações de base contínua nativa no formato unificado passiva autônomo.
Inspeção vital indicando fluxo regular sadio pontual acompanhando aberturas ou limitações expostas integradas do modelo defensivo via relatórios da arquitetura central Resilience4j Circuit Breaker responsável pelas ligações remotas via Feign entre canais de contato acoplados na interface remota isolada gerencial.
Contagem e fluxo saudável total e estabilidade englobada base nos disparos, fluxos e emissão do broker de dados acoplado ActiveMQ garantido nas trocas logísticas de mensagerias sem acoplamento de base central remota livre nativa independente unida autônoma e autônoma logística unida remota passiva integrada ativa remota nativa central.

10. ADR Relacionado
Adoção formal da técnica de arquitetura sistêmica distribuída inteligente para atuar de formato misto de acordo base de separação na via engenhosa logística passiva: O microsserviço isolado consome catálogos dependentes via chamada REST fechada direta HTTP síncrona englobada ao Spring Feign, contudo emite reações drásticas e avisos graves passivamente atirando e empurrando o sinal crítico às cegas na via TCP assíncrona base nativa JMS eliminando travas operacionais e desacoplando seu destino logístico sem esperar de modo passivo-agressivo que outros leiam do aviso remoto isolado unificado na base logístico.
