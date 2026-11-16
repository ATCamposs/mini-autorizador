---
status: accepted
date: 2026-11-16
decision-makers: André Campos
---

# Debitar o saldo com UPDATE condicional atômico

## Contexto e problema

A aplicação pode rodar em várias instâncias sobre o mesmo MySQL, e duas transações do mesmo cartão
podem chegar juntas em instâncias diferentes. Ler o saldo, conferir no Java e gravar perde
atualização e deixa o saldo negativo. Lock em memória não vale entre instâncias.

## Opções consideradas

- `UPDATE cartao SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?`, aprovando se 1 linha mudou: escolhida
- `SELECT ... FOR UPDATE`, conferir e `UPDATE`: rejeitada porque segura o lock da linha durante a transação inteira e exige transação explícita em volta da leitura
- Lock otimista com `@Version`: rejeitada porque quem perde o conflito precisa de retry ou recebe um erro que o contrato não tem
- Lock distribuído (Redis): rejeitada porque traz infraestrutura que o compose do desafio não tem

## Decisão

Escolhemos o `UPDATE` condicional, porque o banco, a única fonte comum às instâncias, checa o saldo e
debita numa operação só.

### Consequências

- Bom: nenhum lock segurado entre ler e gravar, nenhum retry, e funciona com N instâncias.
- Ruim: a regra de saldo vive no SQL, não no Java. A resposta é só "debitou ou não", sem o saldo restante.
- Ruim: "1 linha" só significa débito porque `valor > 0` é validado na entrada; o Connector/J conta linhas encontradas, não alteradas.
- Gatilho para revisar: a transação passar a gravar histórico ou a mexer em mais de uma linha, como um ledger.

### Confirmação

O `ConcorrenciaTest` dispara 60 transações simultâneas contra R$ 500,00 e exige 50 aprovadas; ele mata a
mutação "ler, conferir e gravar".
