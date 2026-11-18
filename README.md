# Circuito Zero Desperdício - Microsserviços

Global Solution 2026/2 - FIAP - Microservice and Web Engineering

## Integrantes

| Nome | RM |
|---|---|
| Davi Fiszbejn | RM557716 |
| Leonardo da Silva de Oliveira | RM557333 |
| Augusto Tomaselli | RM555916 |

## Problema

Restaurantes, padarias e pequenos comércios de bairro descartam alimentos em perfeitas condições no fim do expediente, enquanto famílias vulneráveis e ONGs da mesma região enfrentam insegurança alimentar. O maior obstáculo para a doação é a logística: a sobra aparece no fim do turno e precisa ser retirada rapidamente.

## Solução

Uma plataforma de redistribuição rápida de excedentes alimentares do comércio de bairro. O comerciante cadastra o excedente disponível (ex.: 20 pães e 5 marmitas) e instituições próximas reservam e retiram a doação a pé ou em trajetos curtos.

O backend é formado por dois microsserviços:

- **ms-ofertas**: cadastro das ofertas de excedentes e controle do status da oferta (`DISPONIVEL`, `RESERVADA`, `RETIRADA`, `CANCELADA`).
- **ms-resgates**: reservas e retiradas feitas pelas instituições, com status do resgate (`RESERVADO`, `RETIRADO`, `CANCELADO`, `CONFIRMACAO_PENDENTE`).

Quando uma instituição faz um resgate, o ms-resgates chama o ms-ofertas via **OpenFeign** (com Eureka e Load Balancer) e o status da oferta muda para `RESERVADA`. Se o ms-ofertas estiver fora do ar, o **Circuit Breaker** aciona o **Fallback**: o resgate é salvo como `CONFIRMACAO_PENDENTE` e a resposta é `503`.

## Tecnologias e portas

| Projeto | Tecnologia | Porta |
|---|---|---|
| eureka-server | Spring Boot 3.5 / Eureka Server | 8761 |
| api-gateway | Spring Boot 3.5 / Spring Cloud Gateway | 8082 |
| ms-ofertas | Java 25 / Spring Boot 4.0.9 / JPA / Flyway / MySQL | aleatória |
| ms-resgates | Java 25 / Spring Boot 4.0.9 / JPA / Flyway / MySQL / OpenFeign / Resilience4j | aleatória |
| MySQL | MySQL 8.4.6 (Docker) | 3306 |

Cada microsserviço tem seu próprio banco: `db_ofertas` e `db_resgates`. Os bancos, as tabelas e os dados iniciais são criados automaticamente pelo Flyway.

## Como executar

### Pré-requisitos

- JDK 25
- Docker Desktop
- Portas 8761, 8082 e 3306 livres

### 1. Clonar o repositório

```bash
git clone https://github.com/LeonardoSilvaOliveira/circuito-zero-microservices.git
cd circuito-zero-microservices
```

### 2. Subir o MySQL com Docker

```bash
docker compose up -d
```

Aguarde cerca de 40 segundos para o MySQL iniciar (usuário `root`, senha `root`). Só inicie os microsserviços depois que o MySQL estiver no ar. Se já houver outro MySQL usando a porta 3306 (como o das aulas), pare-o antes de rodar o `docker compose up -d`.

### 3. Subir os serviços (nesta ordem, cada um em um terminal)

```bash
cd eureka-server && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
cd ms-ofertas && ./mvnw spring-boot:run
cd ms-resgates && ./mvnw spring-boot:run
```

No Windows, use `.\mvnw.cmd spring-boot:run` (PowerShell) ou `mvnw.cmd spring-boot:run` (CMD). Também é possível executar as classes `*Application` pelo IntelliJ.

Para testar o Load Balancer, suba uma segunda instância do ms-ofertas repetindo o comando em outro terminal (os microsserviços usam porta aleatória).

### 4. Conferir

- Eureka: http://localhost:8761 (devem aparecer API-GATEWAY, MS-OFERTAS e MS-RESGATES)
- Swagger: http://localhost:8082/swagger-ui/index.html (selecione ms-ofertas ou ms-resgates no canto superior direito)

### 5. Encerrar

```bash
docker compose down
```

## Endpoints

Todos acessíveis pelo Gateway: `http://localhost:8082/{servico}/api/v1/...`

### ms-ofertas - `/ms-ofertas/api/v1/ofertas`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/ofertas` | lista as ofertas (filtro opcional `?status=DISPONIVEL`) |
| GET | `/api/v1/ofertas/{id}` | busca por ID |
| POST | `/api/v1/ofertas` | cadastra oferta (status inicial `DISPONIVEL`) |
| PUT | `/api/v1/ofertas/{id}` | atualiza oferta |
| DELETE | `/api/v1/ofertas/{id}` | exclui oferta |
| PATCH | `/api/v1/ofertas/{id}/reservar` | `DISPONIVEL` para `RESERVADA` (usado pelo ms-resgates) |
| PATCH | `/api/v1/ofertas/{id}/retirar` | `RESERVADA` para `RETIRADA` (usado pelo ms-resgates) |
| PATCH | `/api/v1/ofertas/{id}/liberar` | `RESERVADA` para `DISPONIVEL` (usado pelo ms-resgates) |
| PATCH | `/api/v1/ofertas/{id}/cancelar` | `DISPONIVEL` para `CANCELADA` |
| GET | `/api/v1/ofertas/instancia` | mostra a porta da instância (teste do Load Balancer) |

### ms-resgates - `/ms-resgates/api/v1/resgates`

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/resgates` | lista os resgates (filtro opcional `?status=RESERVADO`) |
| GET | `/api/v1/resgates/{id}` | busca por ID |
| POST | `/api/v1/resgates` | reserva uma oferta (chama o ms-ofertas) |
| PUT | `/api/v1/resgates/{id}` | atualiza os dados do resgate |
| DELETE | `/api/v1/resgates/{id}` | exclui resgate |
| PATCH | `/api/v1/resgates/{id}/retirar` | `RESERVADO` para `RETIRADO` (chama o ms-ofertas) |
| PATCH | `/api/v1/resgates/{id}/cancelar` | cancela o resgate e a oferta volta a `DISPONIVEL` |
| PATCH | `/api/v1/resgates/{id}/confirmar` | `CONFIRMACAO_PENDENTE` para `RESERVADO` (chama o ms-ofertas) |

Exemplo de corpo para criar um resgate:

```json
{
  "ofertaId": 2,
  "instituicaoNome": "Cozinha Solidaria Se",
  "responsavelNome": "Carlos",
  "telefone": "11999998888"
}
```

## Teste do Circuit Breaker

1. Pare todas as instâncias do ms-ofertas.
2. Faça um `POST /ms-resgates/api/v1/resgates`: a resposta é `503` e o resgate é salvo como `CONFIRMACAO_PENDENTE`.
3. Suba o ms-ofertas novamente, aguarde cerca de 90 segundos e chame `PATCH /ms-resgates/api/v1/resgates/{id}/confirmar`: o resgate passa para `RESERVADO` e a oferta para `RESERVADA`. Logo depois de derrubar ou subir uma instância, o Gateway pode alternar entre respostas 200 e 500 por até 1 minuto e meio, até o Eureka atualizar o registro. Isso é normal.
