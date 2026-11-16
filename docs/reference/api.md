# API

Base: `http://localhost:8080`. Corpos de requisição em JSON.

## POST /cartoes

| Campo | Formato |
| --- | --- |
| `numeroCartao` | 13 a 19 dígitos |
| `senha` | 1 a 72 caracteres ASCII imprimíveis |

| Status | Corpo | Quando |
| --- | --- | --- |
| 201 | `{"senha":"…","numeroCartao":"…"}`, o JSON recebido | cartão criado com saldo `500.00` |
| 422 | o JSON recebido | o número já existe |
| 400 | corpo padrão do Spring | campo ausente ou fora do formato |

## GET /cartoes/{numeroCartao}

| Status | Corpo | Quando |
| --- | --- | --- |
| 200 | saldo como número JSON com duas casas, ex. `495.15` | cartão existe |
| 404 | vazio | número não cadastrado ou fora do formato |

## POST /transacoes

| Campo | Formato |
| --- | --- |
| `numeroCartao` | 13 a 19 dígitos |
| `senhaCartao` | 1 a 72 caracteres ASCII imprimíveis |
| `valor` | positivo, até 2 casas decimais e até 17 dígitos inteiros |

| Status | Corpo (`text/plain`) | Quando |
| --- | --- | --- |
| 201 | `OK` | débito feito |
| 422 | `CARTAO_INEXISTENTE` | o número não existe |
| 422 | `SENHA_INVALIDA` | o cartão existe e a senha não confere |
| 422 | `SALDO_INSUFICIENTE` | senha correta e saldo menor que o valor |
| 400 | corpo padrão do Spring | campo ausente ou fora do formato |

Quando mais de uma regra barra, vale a primeira na ordem da tabela. A resposta nunca é 406, mesmo com
`Accept: application/json`.
