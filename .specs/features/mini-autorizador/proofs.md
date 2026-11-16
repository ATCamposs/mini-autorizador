# mini-autorizador proofs

**HEAD**: 8cdb96a
**Tree**: clean
**Result**: 30 pass, 0 fail; mutations 3 killed, 0 not

| Check | Proof | Ran | Passed | Result |
| --- | --- | --- | --- | --- |
| C1 | `./mvnw -q test -Dtest='CartaoApiTest#criaCartaoNovoResponde201ComEcoNaOrdemDoContrato'` | count unknown | - | pass |
| C2 | `./mvnw -q test -Dtest='CartaoApiTest#cartaoNasceComSaldo500'` | count unknown | - | pass |
| C3 | `./mvnw -q test -Dtest='CartaoApiTest#numeroRepetidoResponde422ENaoSobrescreve'` | count unknown | - | pass |
| C4 | `./mvnw -q test -Dtest='CartaoApiTest#doisPostsSimultaneosCriamUmCartaoSo'` | count unknown | - | pass |
| C5 | `./mvnw -q test -Dtest='CartaoApiTest#saldoComDuasCasas'` | count unknown | - | pass |
| C6 | `./mvnw -q test -Dtest='CartaoApiTest#saldoDeCartaoInexistenteResponde404SemCorpo'` | count unknown | - | pass |
| C7 | `./mvnw -q test -Dtest='CartaoApiTest#senhaGuardadaSoComoHash'` | count unknown | - | pass |
| C8 | `./mvnw -q test -Dtest='TransacaoApiTest#transacaoAprovadaDebitaEResponde201Ok'` | count unknown | - | pass |
| C9 | `./mvnw -q test -Dtest='TransacaoApiTest#saldoInsuficienteResponde422SemDebitar'` | count unknown | - | pass |
| C10 | `./mvnw -q test -Dtest='TransacaoApiTest#senhaInvalidaResponde422SemDebitar'` | count unknown | - | pass |
| C11 | `./mvnw -q test -Dtest='TransacaoApiTest#cartaoInexistenteResponde422'` | count unknown | - | pass |
| C12 | `./mvnw -q test -Dtest='AutorizacaoTest'` | count unknown | - | pass |
| C12 | `./mvnw -q test -Dtest='TransacaoApiTest#senhaErradaComSaldoInsuficienteRespondeSenhaInvalida'` | count unknown | - | pass |
| C13 | `./mvnw -q test -Dtest='ConcorrenciaTest'` | count unknown | - | pass |
| C14 | `bash -c "! rg -n -w -e if -e break -e continue src/main/java/br/com/miniautorizador/transacao"` | count unknown | - | pass |
| C15 | `./mvnw -q test -Dtest='TransacaoApiTest#acceptJsonNaoDa406'` | count unknown | - | pass |
| C16 | `bash -c "! rg -n -w -e double -e float -e Double -e Float src/main/java"` | count unknown | - | pass |
| C17 | `./mvnw -q test -Dtest='TransacaoApiTest#dezCentavosSomamExato'` | count unknown | - | pass |
| C18 | `./mvnw -q test -Dtest='TransacaoApiTest#valorInvalidoResponde400'` | count unknown | - | pass |
| C19 | `./mvnw -q test -Dtest='ValidacaoPayloadTest#numeroCartaoForaDoFormato'` | count unknown | - | pass |
| C20 | `./mvnw -q test -Dtest='ValidacaoPayloadTest#senhaForaDoFormato'` | count unknown | - | pass |
| C21 | `./mvnw -q test -Dtest='PartidaTest#schemaCriadoNaPartidaEIdempotente'` | count unknown | - | pass |
| C22 | `walk - docker compose -f docker/docker-compose.yml up -d mysql, ./mvnw spring-boot:run, e o roteiro do docs/de` | - | - | pass (Walked) |
| C23 | `./mvnw -q test -Dtest='PartidaTest#esperaOMysqlPor60Segundos'` | count unknown | - | pass |
| C24 | `bash -c "diff <(sed -n '/^  mysql:/,\$p' docs/docker-compose.original.yml) <(sed -n '/^  mysql:/,\$p' docker/d` | count unknown | - | pass |
| C24 | `bash -c "! rg -n '^  mongo:' docker/docker-compose.yml"` | count unknown | - | pass |
| C25 | `bash -c "rg -q '^## Como rodar' README.md && rg -q '^## Como testar' README.md && rg -q '^## Suposições' READM` | count unknown | - | pass |
| C26 | `bash -c "python3 -I .specs/features/mini-autorizador/assumptions_no_readme.py .specs/features/mini-autorizador` | count unknown | - | pass |
| C27 | `./mvnw -q test -Dtest='TratamentoErrosTest#soChaveDuplicadaVira422'` | count unknown | - | pass |
| Build | `./mvnw -q -DskipTests package` | - | - | pass |

## Mutations

| Mutation | Location | Killed |
| --- | --- | --- |
| débito atômico -> ler saldo, conferir em Java e gravar saldo absoluto (lost update) | `src/main/java/br/com/miniautorizador/cartao/CartaoRepository.java:34` | yes |
| saldo >= valor -> saldo > valor (saldo igual ao valor recusado) | `src/main/java/br/com/miniautorizador/cartao/CartaoRepository.java:34` | yes |
| catch DuplicateKeyException -> DataIntegrityViolationException (qualquer violação vira 422) | `src/main/java/br/com/miniautorizador/cartao/CartaoService.java:28` | yes |
