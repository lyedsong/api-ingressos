# API de Ingressos — Alta Demanda

API REST desenvolvida em Java com Spring Boot e PostgreSQL para simular um sistema de venda e reserva de ingressos sob alta demanda.

O projeto implementa autenticação com JWT, controle de estoque, limite de reservas por usuário e proteção contra reservas simultâneas que ultrapassem a quantidade disponível.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Token)
- Docker e Docker Compose
- Maven

## Como executar o projeto

A aplicação e o banco de dados são executados em containers Docker.

### Pré-requisitos

- Docker Desktop instalado e em execução.
- Git instalado.

Não é necessário instalar Java, Maven ou PostgreSQL localmente para executar a aplicação pelo Docker Compose.

### 1. Clonar o repositório

```bash
git clone https://github.com/lyedsong/api-ingressos.git
```

### 2. Entrar na pasta

```bash
cd api-ingressos
```

### 3. Iniciar a aplicação

```bash
docker compose down -v
docker compose up --build -d
```

A aplicação ficará disponível em:

`http://localhost:8080`

O PostgreSQL será iniciado automaticamente pelo Docker Compose.

**Atenção:** o comando `docker compose down -v` remove também o volume do banco de dados, apagando os dados armazenados. Utilize-o quando desejar iniciar uma base limpa.

### 4. Verificar se a API está funcionando

```bash
Invoke-RestMethod -Uri "http://localhost:8080/pool" -Method Get
```

Resposta esperada inicialmente:

```json
{
  "total": 100,
  "ocupadas": 0,
  "livres": 100
}
```

## Regras de negócio

- O sistema disponibiliza exatamente 100 ingressos.
- Cada usuário pode ter no máximo 2 reservas ativas.
- Uma reserva só é confirmada quando existe ingresso disponível e o usuário respeita seu limite pessoal.
- Pedidos começam com o status `NO_CARRINHO`.
- Pedidos reservados passam para `RESERVADO`.
- Pedidos cancelados passam para `CANCELADO`.
- O cancelamento devolve o ingresso ao estoque.
- Um pedido só pode ser alterado pelo usuário que o criou.

### Fluxo de status

`NO_CARRINHO → RESERVADO → CANCELADO`

## Endpoints da API

### Autenticação

As rotas de autenticação são públicas.

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/registrar` | Cadastrar usuário |
| POST | `/auth/login` | Realizar login e obter token JWT |

**Cadastrar usuário**

Requisição:

```json
{
  "username": "joao",
  "password": "senha123"
}
```

Endpoint: `POST /auth/registrar`

Resposta esperada: HTTP 201.

**Realizar login**

Requisição:

```json
{
  "username": "joao",
  "password": "senha123"
}
```

Endpoint: `POST /auth/login`

Resposta:

```json
{
  "token": "TOKEN_JWT"
}
```

O token deve ser utilizado nas requisições protegidas.

### Pedidos

Todas as rotas de pedidos exigem autenticação via JWT.

Cabeçalho:

```http
Authorization: Bearer TOKEN_JWT
```

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/pedidos` | Criar pedido |
| POST | `/pedidos/{id}/reservar` | Reservar ingresso |
| POST | `/pedidos/{id}/cancelar` | Cancelar reserva |

**Criar pedido**

Endpoint: `POST /pedidos`

Requisição:

```json
{
  "showId": 7
}
```

Resposta esperada: HTTP 201.

```json
{
  "id": 1,
  "status": "NO_CARRINHO"
}
```

**Reservar ingresso**

Endpoint: `POST /pedidos/{id}/reservar`

Resposta de sucesso: HTTP 200.

```json
{
  "id": 1,
  "status": "RESERVADO"
}
```

A resposta de sucesso também contém o cabeçalho:

```http
X-Lote: 7F3A
```

**Cancelar reserva**

Endpoint: `POST /pedidos/{id}/cancelar`

Resposta: HTTP 200.

```json
{
  "id": 1,
  "status": "CANCELADO"
}
```

### Consulta do estoque

Endpoint público:

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/pool` | Consultar quantidade de ingressos |

Exemplo de resposta:

```json
{
  "total": 100,
  "ocupadas": 37,
  "livres": 63
}
```

A soma de ingressos ocupados e livres deve permanecer igual a 100.

## Tratamento de erros

A API utiliza códigos HTTP adequados para representar situações de conflito e falhas de autenticação.

| Código HTTP | Situação | Resposta |
|---|---|---|
| 401 | Usuário não autenticado | Não autorizado |
| 404 | Pedido inexistente ou pertencente a outro usuário | Não encontrado |
| 409 | Estoque esgotado | `{"error":"POOL_CHEIO"}` |
| 409 | Usuário já possui 2 reservas ativas | `{"error":"COTA_PESSOAL"}` |

## Controle de concorrência

Para evitar que várias requisições simultâneas reservem o mesmo ingresso ou ultrapassem o limite de estoque, o projeto utiliza controle transacional com bloqueio pessimista de escrita no PostgreSQL.

A estratégia funciona da seguinte forma:

1. A transação solicita um bloqueio na linha do estoque.
2. O sistema verifica se o pedido pertence ao usuário.
3. Verifica se o pedido pode ser reservado.
4. Confere o limite de duas reservas ativas por usuário.
5. Confere a quantidade disponível no estoque.
6. Se todas as condições forem atendidas, atualiza o estoque e o status do pedido.
7. Caso alguma condição não seja atendida, a operação é interrompida e a API retorna o erro correspondente.

O bloqueio impede que duas transações modifiquem simultaneamente o mesmo registro de estoque, protegendo a quantidade de ingressos e o limite individual de reservas.

As operações de reserva e cancelamento são executadas com `@Transactional`, garantindo que as alterações relacionadas sejam confirmadas ou revertidas em conjunto.

## Estrutura do projeto

```text
api-ingressos/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/projeto/apiingressos/
│   │   │   ├── auth/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .dockerignore
├── .gitignore
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Autor

Projeto acadêmico desenvolvido como atividade prática de construção de uma API REST sob alta demanda.