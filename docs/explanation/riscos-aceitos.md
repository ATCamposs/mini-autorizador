# Riscos aceitos

Três riscos ficaram fora do escopo do desafio. Cada um vem com o que faríamos para tratá-lo.

## Reenvio duplica o débito

O contrato não tem id de transação, então um retry depois de um timeout é uma transação nova e debita
de novo. Como resolveríamos: um id de idempotência no payload, gravado com chave única na mesma
operação do débito.

## Força bruta da senha

Uma senha de 4 dígitos tem 10 mil combinações, e não há bloqueio por tentativas. O hash BCrypt protege
o banco, mas não a API. Como resolveríamos: contador de tentativas por cartão com bloqueio temporário,
e limite de requisições por origem.

## Número do cartão em claro

O contrato exige ecoar o número, e ele é a chave da tabela. Como resolveríamos: tokenizar o número,
com o cofre fora da aplicação e dentro do escopo PCI.
