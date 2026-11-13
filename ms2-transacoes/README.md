# MS2 — Serviço de Transações

Microsserviço responsável pelo registro e consulta de transações financeiras (depósito, saque, transferência e compra), validação de saldo e limites, conversão de valores em moeda estrangeira, análise de despesas e geração de relatório. Consome o MS1 para autenticação e existência de usuários, a API Mock para saldo/limites de conta e a BrasilAPI para cotação de câmbio.

### O que o serviço faz

- Criação de transações dos tipos **DEPOSITO**, **SAQUE**, **TRANSFERENCIA** e **COMPRA** (`POST /api/transacoes`), cada uma terminando em status **APROVADA** ou **REJEITADA**, com o motivo da rejeição quando aplicável.
- Transações **nunca são apagadas** — mesmo rejeitadas, ficam gravadas no histórico do usuário.
- Consulta de uma transação pelo identificador (`GET /api/transacoes/{id}`) e listagem das transações do usuário autenticado, com filtro por período e por tipo (`GET /api/transacoes/usuario`).
- Consulta do saldo atual do usuário, obtido em tempo real na API Mock (`GET /api/transacoes/saldo`).
- Validação de saldo, limite por transação e limite diário antes de aprovar saque, transferência ou compra — depósito não passa por essa validação.
- Em transferências, débito na conta de origem e crédito na conta de destino; se o crédito falhar, o débito é **compensado automaticamente** e a transação fica marcada como rejeitada.
- Conversão do valor de cada transação para **USD** e **EUR** na consulta, usando a cotação de venda do dia (ou do dia mais recente disponível, em até 7 dias) da BrasilAPI. Se a cotação não estiver disponível, a consulta da transação responde normalmente, sinalizando a indisponibilidade da conversão (`disponivel: false`).
- Análise de despesas por categoria, por dia e por mês em um período (`GET /api/transacoes/despesas`) — considera apenas transações aprovadas de saída (saque, transferência e compra); por padrão, quando o período não é informado, usa o mês atual.
- Geração de relatório de transações em CSV para download (`GET /api/transacoes/relatorio`), compatível com Excel em português (separador `;`, BOM UTF-8 e vírgula decimal).
- Todas as rotas de transação, saldo, despesas e relatório exigem usuário autenticado; a documentação (Swagger) fica aberta.
- Um usuário autenticado só acessa as próprias transações — nunca as de outro usuário.

### Arquitetura (hexagonal)

---

```
src/main/java/com/apigestaofinanceira/transacoes/
├── domain/
│   ├── model/                 -> Transacao, TipoTransacao, StatusTransacao (entidades de domínio, sem dependência de framework)
│   └── ports/output/          -> TransacaoRepositoryPort, ContaClientPort, CambioClientPort, UsuarioClientPort (portas de saída)
├── application/
│   └── usecases/               -> CreateTransacaoUseCase, FindTransacaoUseCase, ListTransacoesUseCase,
│                                  AnalisarDespesasUseCase, ConverterValorTransacaoUseCase, GerarRelatorioCsvUseCase
└── infrastructure/
    ├── adapters/
    │   ├── input/web/          -> TransacaoController, DTOs e TransacaoWebMapper (porta de entrada HTTP)
    │   └── output/
    │       ├── persistence/    -> Adapter JPA, entidade, mapper e repositório Spring Data (banco de transações)
    │       ├── mockapi/        -> MockApiContaClient (saldo e limites de conta)
    │       ├── brasilapi/      -> BrasilApiCambioClient (cotação de câmbio)
    │       └── ms1/            -> Ms1UsuarioClient (existência de usuário)
    └── config/
        ├── security/            -> SecurityConfig, Ms1AuthenticationProvider, TitularidadeFilter, UsuarioAutenticado
        ├── exception/           -> GlobalExceptionHandler, UsuarioNaoEncontradoException
        └── Ms1ClientConfig.java -> Configuração dos RestClient (MS1, Mock API, BrasilAPI)
```

O domínio (`domain/model/Transacao`) não depende de Spring, JPA ou HTTP — toda a regra de negócio (validação de tipo, saldo, limites, compensação de débito) fica isolada nos casos de uso (`application/usecases`). As portas de saída (`ContaClientPort`, `CambioClientPort`, `UsuarioClientPort`, `TransacaoRepositoryPort`) permitem trocar a API Mock, a BrasilAPI, o MS1 ou o banco de dados sem alterar a regra de negócio.

### Como subir com o Docker Compose

O MS2 sobe junto com o MS1, o banco de usuários, o banco de transações e o pgAdmin a partir da raiz do projeto:

```
docker compose up -d
```

- API de transações: http://localhost:5002
- Swagger: http://localhost:5002/swagger-ui.html

Para derrubar o ambiente:

```
docker compose down
```

### Como rodar os testes

---

```
./mvnw test
```

Os testes cobrem as regras críticas do domínio e da infraestrutura: aprovação e rejeição de transação (saldo insuficiente, limite por transação, limite diário), depósito sem validação de limites, transferência com atualização das duas contas e compensação de débito em caso de falha no crédito, análise de despesas, conversão de câmbio, geração do relatório CSV, mapeamento de DTOs/entidades, integração com a API Mock, com a BrasilAPI e com o MS1, além da autenticação delegada ao MS1 e do filtro de titularidade.

O build gera um relatório de cobertura (JaCoCo) em `target/site/jacoco/index.html`.

### Variáveis de ambiente

---

Definidas no arquivo `.env` na raiz do projeto (veja `env.example`):

| Variável | Descrição |
|---|---|
| TRANSACTION_DB_NAME | Nome do banco de dados de transações |
| TRANSACTION_DB_USER | Usuário do banco de transações |
| TRANSACTION_DB_PASSWORD | Senha do banco de transações |
| MS1_BASE_URL | URL base do MS1, usada para autenticação e verificação de usuário |
| BRASILAPI_BASE_URL | URL base da BrasilAPI, usada na consulta de câmbio |

Quando executado fora do Docker Compose, o serviço espera o Postgres em `localhost:5434` e o MS1 em `localhost:5001` (ver `application.properties`).

### Configuração da API Mock

---

A API Mock (saldo e limites de conta) é consumida apenas pelo MS2, configurada em `application.properties` na propriedade `mockapi.base-url`. Cada conta possui `saldo`, `limiteDiario` e `limitePorTransacao`, indexados pelo `usuarioId`. O MS2 consulta essa API antes de aprovar saque, transferência ou compra, e atualiza o saldo após a aprovação.

### Endpoints principais

---

| Método | Rota | Descrição |
|---|---|---|
| POST | /api/transacoes | Cria uma transação (depósito, saque, transferência ou compra) |
| GET | /api/transacoes/{id} | Busca uma transação do usuário autenticado pelo identificador |
| GET | /api/transacoes/usuario | Lista as transações do usuário autenticado, com filtro por período e tipo |
| GET | /api/transacoes/saldo | Consulta o saldo atual do usuário autenticado |
| GET | /api/transacoes/despesas | Analisa as despesas do usuário por categoria, dia e mês em um período |
| GET | /api/transacoes/relatorio | Gera um relatório em CSV com as transações do usuário autenticado |

Todas as rotas acima exigem autenticação (Basic Auth, validada contra o MS1) e o cabeçalho `X-Usuario-Id`, que deve corresponder ao usuário autenticado — caso contrário a requisição é rejeitada com `403 Forbidden`. A documentação (`/swagger-ui.html`, `/v3/api-docs/**`) fica aberta, sem autenticação.
