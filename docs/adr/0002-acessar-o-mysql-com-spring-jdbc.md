---
status: accepted
date: 2026-11-16
decision-makers: André Campos
---

# Acessar o MySQL com Spring JDBC, sem JPA

## Contexto e problema

O compose do desafio fixa o MySQL 5.7 e não pode mudar. O Hibernate 6 e 7, que vêm no Spring Boot 4,
só suportam MySQL 8.0 ou mais novo e avisam com HHH000511. Além disso, o número do cartão é o id, e o
`save()` do Spring Data trata id preenchido como registro existente: faria merge e sobrescreveria um
cartão já criado.

## Opções consideradas

- `JdbcClient` com SQL escrito à mão: escolhida
- Spring Data JPA com o `MySQLDialect` padrão: rejeitada porque roda fora da versão suportada
- JPA com o dialeto 5.7 do `hibernate-community-dialects`: rejeitada porque é dialeto sem suporte oficial, é mais uma dependência e ainda exige contornar o merge com `Persistable`
- Spring Data JDBC: rejeitada porque também trata id preenchido como registro existente e faz `UPDATE` em vez de `INSERT`

## Decisão

Escolhemos o `JdbcClient`, porque uma tabela e três comandos não pedem ORM, e o SQL explícito deixa à
vista o `INSERT` que falha na chave duplicada e o `UPDATE` condicional da [ADR-0001](0001-debitar-o-saldo-com-update-condicional.md).

### Consequências

- Bom: nenhuma dependência de dialeto, e comandos explícitos.
- Ruim: o mapeamento de linha é feito à mão, e mudança de schema é SQL manual, sem os recursos de um ORM.
- Gatilho para revisar: banco em 8.0 ou mais novo, ou um modelo com várias entidades relacionadas.

### Confirmação

O `CartaoRepository` é a única classe com SQL, e o `TratamentoErrosTest` prova que só a chave duplicada
vira 422.
