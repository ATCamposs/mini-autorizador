# Mini autorizador

API REST em Spring Boot que cria cartões de benefício, consulta saldo e autoriza transações, conforme o
[desafio da VR Benefícios](docs/desafio.md). As transações não são persistidas; o cartão e o saldo, sim.

## Início rápido

Requisitos: JDK 17 ou mais novo e Docker.

```bash
docker compose -f docker/docker-compose.yml up -d mysql
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Para os testes, que sobem um `mysql:5.7` pelo Testcontainers:

```bash
./mvnw test
```

## Destaques

- **Concorrência entre instâncias:** o débito é um `UPDATE` condicional, e o banco decide; 60 transações
  simultâneas contra R$ 500,00 aprovam exatamente 50 ([ADR-0001](docs/adr/0001-debitar-o-saldo-com-update-condicional.md)).
- **Regras sem `if`:** cartão, senha e saldo, nessa ordem ([explicação](docs/explanation/regras-de-autorizacao.md)).
- **MySQL 5.7 do desafio:** Spring JDBC no lugar de JPA ([ADR-0002](docs/adr/0002-acessar-o-mysql-com-spring-jdbc.md)).

## Documentação

O [índice](docs/README.md) reúne como rodar, o roteiro do desafio, a API, as suposições, os riscos
aceitos e as decisões.
