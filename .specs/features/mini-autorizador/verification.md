# Mini autorizador verification

**Verdict**: FAIL
**Mutations**: 3 - probed C13, C8, C27 (3 killed)
**Proofs**: `proofs.md` at 0865207 (30 pass, 0 fail, tree clean)
**Verifier**: independent sub-agent (author != verifier)

## Mutations injected

| Mutation | Location | Killed |
| --- | --- | --- |
| débito atômico -> ler saldo, conferir em Java e gravar saldo absoluto (lost update) | `CartaoRepository.java:34`, proof `ConcorrenciaTest` | yes |
| `saldo >= valor` -> `saldo > valor` | `CartaoRepository.java:34`, proof `TransacaoApiTest#transacaoAprovadaDebitaEResponde201Ok` | yes |
| `catch DuplicateKeyException` -> `DataIntegrityViolationException` | `CartaoService.java:28`, proof `TratamentoErrosTest#soChaveDuplicadaVira422` | yes |

## Own examples

- AC 6 (GET de número inexistente -> 404): `GET /cartoes/<numero cadastrado>%20` e `%20%20%20` -> **200 `500.00`** (ver finding 1).
- AC 15 (valor com mais de duas casas -> 400, nunca arredonda): `10.000` -> 400, `0.010` -> 400, `1e2` -> 201 e debita exatamente `100.00`, `"5.00"` (string) -> 201, `99999999999999999.99` -> 422 `SALDO_INSUFICIENTE` sem alterar saldo. Sem achado.

## Findings

| Finding | Evidence | Fix | Closed |
| --- | --- | --- | --- |
| `GET /cartoes/{numeroCartao}` com espaço no fim devolve o saldo de outro número, em vez de 404 (AC 6: número não cadastrado -> 404) | exemplo próprio a 0865207: cartão `N` criado, `GET /cartoes/N%20` -> 200 `500.00`; o `WHERE numero = ?` (`CartaoRepository.java:26`) compara `VARCHAR` com PAD SPACE na collation padrão do MySQL 5.7, que ignora espaço à direita | comparar sem padding (ex. `WHERE numero = ? AND LENGTH(numero) = LENGTH(?)`, `BINARY`, ou recusar no controller o que não é `\d{13,19}` com 404) e um teste em `saldoDeCartaoInexistenteResponde404SemCorpo` com `N%20` de um número cadastrado || `7decbc6` |
| C5 assere o texto `500.00`/`495.15`, não que o saldo sai como número JSON (AC 5) | `CartaoApiTest.java:80-81` só compara `body()`; um `ResponseEntity<String>` com `text/plain` passaria | assertar também `Content-Type` `application/json` na resposta do GET || `7decbc6` |
