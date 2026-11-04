# MS1 — Serviço de Usuários
Microsserviço responsável pelo cadastro, consulta, atualização e exclusão de usuários da plataforma, além da autenticação usada pelo MS2 (serviço de transações).

### O que o serviço faz
- Cadastro individual de usuário (POST /api/usuarios).
- Cadastro em massa de usuários por arquivo CSV (POST /api/usuarios/importar), com relatório de sucesso/falha linha a linha. Um arquivo de exemplo está disponível em exemplo-importacao-usuarios.csv, na raiz deste repositório.
- Consulta, listagem, atualização e exclusão de usuários.
- Autenticação de usuário por email e senha (POST /api/usuarios/autenticar), usada pelo MS2 para validar o Basic Auth das rotas protegidas.
- Senha sempre armazenada com hash (BCrypt) e nunca retornada em nenhuma resposta de API.



### Arquitetura (hexagonal)

---

```
src/main/java/com/apigestao/usuarios/
├── domain/
│   ├── model/              -> Usuario (entidade de domínio, sem dependência de framework)
│   └── ports/output/       -> UsuarioRepositoryPort (porta de saída)
├── application/
│   ├── usecases/           -> Interfaces dos casos de uso (Create, Find, Update, Delete, Autenticar)
│   └── services/           -> UsuarioService (implementação dos casos de uso) e ImportarUsuariosCsvService
└── infrastructure/
├── adapters/
│   ├── input/web/      -> Controller, DTOs e mapper (porta de entrada HTTP)
│   └── output/persistence/ -> Adapter JPA, entidade, mapper e repositório Spring Data
└── config/              -> Configuração de senha (BCrypt) e tratamento global de exceções
```

O domínio (domain/model/Usuario) não depende de Spring, JPA ou HTTP — toda regra de negócio (validação de email duplicado, criação do usuário) fica isolada nos casos de uso (application).

### Como subir com o Docker Compose
O MS1 sobe junto com o MS2, o banco de usuários, o banco de transações e o pgAdmin a partir da raiz do projeto:

```
docker compose up -d
```


- API de usuários: http://localhost:5001
- Swagger: http://localhost:5001/swagger-ui.html


Para derrubar o ambiente:

```
docker compose down
```

### Como rodar os testes

---

```
./mvnw test
```

Os testes cobrem as regras críticas do domínio: cadastro com senha criptografada, impedimento de email duplicado, atualização, exclusão e autenticação (credenciais corretas, senha incorreta, email inexistente).

O build gera um relatório de cobertura (JaCoCo) em target/site/jacoco/index.html.

### Variáveis de ambiente

---

Definidas no arquivo .env na raiz do projeto (veja .env.example):


|Variável|	Descrição|
|---|---|
|USER_DB_NAME	|Nome do banco de dados de usuários |
|USER_DB_USER	|Usuário do banco de usuários|
USER_DB_PASSWORD	|Senha do banco de usuários|
Quando executado fora do Docker Compose, o serviço espera o Postgres em localhost:5433 (ver application.properties).

###  Configuração da API Mock

---

A API Mock (saldo e limites) é consumida apenas pelo MS2. O MS1 não depende dela.

### Endpoints principais

---

|Método	|Rota|	Descrição|
|---|---|---|
|POST	|/api/usuarios/importar	|Importa usuários via CSV|
|POST	|/api/usuarios/autenticar|	Autentica por email/senha (usado pelo MS2)|
|GET	|/api/usuarios|	Lista todos os usuários|
|GET	|/api/usuarios/{id}	|Busca um usuário pelo id|
|PUT	|/api/usuarios/{id}|	Atualiza um usuário|
DELETE	/api/usuarios/{id}	|Exclui um usuário|
Todas as rotas de usuário ficam abertas (sem autenticação) neste serviço — a autenticação é exigida nas rotas do MS2.