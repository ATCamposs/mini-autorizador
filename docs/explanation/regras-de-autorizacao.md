# Sobre as regras de autorização

## Por que sem `if`

O desafio sugere, como opcional, escrever a solução sem `if`, `break` ou `continue`. Aplicamos isso às
três regras de autorização, em `TransacaoService`: cada regra é um `Optional` que recusa com uma
exceção de domínio. O resto do código usa estruturas normais, porque forçar a técnica em configuração e
testes piora a leitura sem ganho.

## Por que a ordem é cartão, senha, saldo

É a ordem em que o enunciado lista as regras, e ela define a resposta quando mais de uma barra: um
cartão inexistente responde `CARTAO_INEXISTENTE` com qualquer senha, e uma senha errada responde
`SENHA_INVALIDA` mesmo sem saldo. A checagem de saldo é o próprio `UPDATE` condicional
([ADR-0001](../adr/0001-debitar-o-saldo-com-update-condicional.md)), então ela só roda depois que as
outras duas passaram.

## Por que dinheiro não usa `double`

`double` e `float` representam frações em binário, e valores como `0.10` não têm representação exata:
dez débitos de `0.10` não somam `1.00`. Usamos `BigDecimal` no Java e `DECIMAL(19,2)` no MySQL, os dois
exatos. Um valor com mais de duas casas é recusado com 400, em vez de arredondado em silêncio.
