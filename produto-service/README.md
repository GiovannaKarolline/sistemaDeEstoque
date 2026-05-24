Documentação do Microsserviço Produto Service

1. Descrição Funcional
Nome do microsserviço: Produto Service
Objetivo e responsabilidades principais: O Produto Service tem o objetivo de gerenciar o catálogo de produtos do sistema de estoque. Sua responsabilidade principal é manter o cadastro dos itens atualizado e ser a fonte oficial de informações. Além disso, ele é responsável por apresentar o preço internacional dos produtos, convertendo o valor original de Reais para Dólares através de uma integração em tempo real com uma API de câmbio.

2. Endpoints da API

Método HTTP: GET
URL: /produtos
Descrição da operação: Retorna a lista de todos os produtos cadastrados no banco de dados, contendo informações básicas e sem realizar a conversão de moedas, a fim de garantir maior velocidade na resposta.

Método HTTP: GET
URL: /produtos/{id}
Descrição da operação: Retorna os dados completos de um único produto, buscando através do seu identificador numérico. Nesta operação, o serviço acessa a API externa para calcular e incluir o valor atual do produto em Dólares na resposta.

Método HTTP: POST
URL: /produtos
Descrição da operação: Recebe os dados de um novo produto e o cadastra no sistema, validando se todas as informações estão corretas e garantindo que o código de estoque (SKU) não está duplicado.

Método HTTP: PUT
URL: /produtos/{id}
Descrição da operação: Atualiza as informações (como nome, descrição e preço) de um produto que já existe no sistema.

Método HTTP: DELETE
URL: /produtos/{id}
Descrição da operação: Exclui permanentemente o cadastro de um produto específico do banco de dados.

3. Exemplo de Requisição e Resposta
Abaixo está o exemplo de como funciona a criação de um novo produto através do endpoint POST /produtos.

Exemplo em JSON de entrada (Requisição):
{
  "nome": "Notebook Gamer",
  "descricao": "Notebook para jogos de alta performance",
  "sku": "NOT-001",
  "precoBrl": 5250.00
}

Exemplo em JSON de saída (Resposta):
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

4. Dependências Externas
Outros microsserviços consumidos: Este serviço atua na base da arquitetura e não consome nenhum outro microsserviço interno.
Banco de dados: Utiliza o banco de dados relacional H2, rodando em memória e gerenciado pelo framework Spring Data JPA.
Fila ou broker de mensagens: Não se aplica. O Produto Service não utiliza filas ou mensageria em sua arquitetura atual.
APIs externas: Consome a API pública AwesomeAPI (economia.awesomeapi.com.br) para consultar a taxa de câmbio mais recente e converter o preço de BRL para USD.

5. Responsável pelo Serviço
Equipe ou pessoa responsável: Desenvolvido e mantido por Giovanna Karolline.

6. Procedimentos Básicos de Operação
Como executar localmente: Abra o terminal, navegue até a pasta do produto-service e digite o comando "mvn spring-boot:run". O serviço ficará disponível na porta 8081.
Como verificar logs: Todos os registros e erros do serviço são impressos diretamente na tela do próprio terminal onde a aplicação foi iniciada.
Endpoint de health check: Para checar se a aplicação está de pé e saudável, acesse a URL /actuator/health no seu navegador.
Como reiniciar o serviço: No terminal em que a aplicação estiver rodando, pressione as teclas Ctrl e C simultaneamente para parar a execução. Após o encerramento ser concluído, digite novamente o comando de inicialização local.

7. Regras de Negócio
Principais validações e comportamentos do serviço:
O sistema valida automaticamente se os dados enviados para criação estão preenchidos corretamente, bloqueando o cadastro de itens com preço negativo ou com campos obrigatórios vazios.
O código identificador do produto, chamado de SKU, deve ser exclusivo. O sistema impede o cadastro de dois itens diferentes compartilhando o mesmo código.
O serviço possui uma regra de proteção (conhecida como Circuit Breaker) na integração com a API de câmbio externa. Se a AwesomeAPI sair do ar, o serviço de produtos não trava. Ele continua operando normalmente, mas passa a retornar o preço em dólar como nulo de forma controlada.

8. Eventos Publicados ou Consumidos
Nome e descrição dos eventos: O Produto Service não faz publicação nem consumo de eventos de mensageria. A sua comunicação ocorre integralmente através de chamadas síncronas HTTP.

9. Métricas Monitoradas
Exemplos de indicadores relevantes:
Quantidade e tempo médio de resposta das chamadas recebidas pelos endpoints da API, o que ajuda a identificar problemas de lentidão.
Monitoramento do estado de comunicação com a API de câmbio externa, permitindo saber se a conexão com o fornecedor terceirizado está falhando com frequência.

10. ADR Relacionado
Decisão arquitetural associada ao serviço: O Produto Service foi isolado com sua própria estrutura de banco de dados, de forma independente do Estoque Service. Essa decisão foi documentada para garantir que qualquer necessidade de consultar os dados de produtos precise ocorrer exclusivamente via rede, garantindo que o banco de dados não seja compartilhado indevidamente entre diferentes microsserviços.
