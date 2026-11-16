# Mini autorizador checks

Mutations: 3
Build: `./mvnw -q -DskipTests package`
Plan: `.specs/features/mini-autorizador/plan.md`

27 checks in 4 slices · 4 one-way doors · 0 open, of which 0 block

## Checks

### S1 - Criar cartão e consultar saldo

**C1** - `POST /cartoes` de número novo responde 201 com `{"senha":"1234","numeroCartao":"6549873025634501"}`, campos nessa ordem (AC 1)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#criaCartaoNovoResponde201ComEcoNaOrdemDoContrato'`

**C2** - Cartão criado tem saldo `500.00` gravado (AC 2)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#cartaoNasceComSaldo500'`

**C3** - Número repetido responde 422 com o JSON recebido; senha e saldo do original não mudam, mesmo com outra senha e depois de gastar (AC 3)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#numeroRepetidoResponde422ENaoSobrescreve'`

**C4** - Dois `POST /cartoes` simultâneos com o mesmo número: um 201, um 422, uma linha na tabela (AC 4)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#doisPostsSimultaneosCriamUmCartaoSo'`

**C5** - `GET /cartoes/{n}` responde 200 com o saldo como número JSON de duas casas: `500.00` recém-criado, `495.15` depois de `4.85` (AC 5)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#saldoComDuasCasas'`

**C6** - `GET /cartoes/{n}` de número inexistente ou malformado (`abc`) responde 404 sem corpo (AC 6)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#saldoDeCartaoInexistenteResponde404SemCorpo'`

**C7** - A coluna `senha_hash` guarda um hash BCrypt que não contém a senha recebida (AC 7)
Proof: `./mvnw -q test -Dtest='CartaoApiTest#senhaGuardadaSoComoHash'`

### S2 - Autorizar transação

**C8** - Cartão existente, senha correta e valor até o saldo: 201 com corpo `OK`, saldo debitado; `500.00 - 10.00 = 490.00` e `10.00 - 10.00 = 0.00` (AC 8)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#transacaoAprovadaDebitaEResponde201Ok'`

**C9** - Valor acima do saldo: 422 `SALDO_INSUFICIENTE` e saldo intacto; saldo `0.00` com `0.01`, saldo `9.99` com `10.00` (AC 9)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#saldoInsuficienteResponde422SemDebitar'`

**C10** - Senha errada: 422 `SENHA_INVALIDA` e saldo intacto (AC 10)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#senhaInvalidaResponde422SemDebitar'`

**C11** - Cartão inexistente, com qualquer senha: 422 `CARTAO_INEXISTENTE` (AC 11)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#cartaoInexistenteResponde422'`

**C12** - Mais de uma regra barrando: vence a primeira na ordem cartão → senha → saldo; senha errada com saldo insuficiente dá `SENHA_INVALIDA` (AC 12)
Proof: `./mvnw -q test -Dtest='AutorizacaoTest'`
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#senhaErradaComSaldoInsuficienteRespondeSenhaInvalida'`

**C13** - Concorrência: saldo `10.00` com duas de `10.00` simultâneas dá uma 201 e uma 422 e saldo `0.00`; saldo `500.00` com 60 de `10.00` simultâneas dá exatamente 50 aprovadas e saldo `0.00`, nunca negativo (AC 13)
Proof: `./mvnw -q test -Dtest='ConcorrenciaTest'`

**C14** - As regras de autorização não usam `if`, `break` nem `continue` (AC 14)
Proof: `bash -c "! rg -n -w -e if -e break -e continue src/main/java/br/com/miniautorizador/transacao"`

**C15** - `POST /transacoes` com `Accept: application/json` responde o mesmo status e corpo puro, nunca 406 (AC 20)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#acceptJsonNaoDa406'`

**C16** - Dinheiro sem `double` nem `float` no código de produção (AC 21)
Proof: `bash -c "! rg -n -w -e double -e float -e Double -e Float src/main/java"`

**C17** - Dez transações de `0.10` a partir de `500.00` deixam o saldo exatamente em `499.00` (AC 21)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#dezCentavosSomamExato'`

### S3 - Recusar payload malformado

**C18** - `valor` ausente, `0`, `-10.00`, `10.001` ou com 18 dígitos inteiros responde 400 sem alterar saldo; `0.01` é aceito (AC 15)
Proof: `./mvnw -q test -Dtest='TransacaoApiTest#valorInvalidoResponde400'`

**C19** - `numeroCartao` com 12 ou 20 dígitos ou com letra responde 400 nos dois POST; 13, 16 e 19 dígitos são aceitos (AC 16)
Proof: `./mvnw -q test -Dtest='ValidacaoPayloadTest#numeroCartaoForaDoFormato'`

**C20** - Senha ausente, vazia, com 73 caracteres ou com `ç` responde 400 nos dois POST; `1234` e 72 caracteres ASCII são aceitos (AC 16)
Proof: `./mvnw -q test -Dtest='ValidacaoPayloadTest#senhaForaDoFormato'`

### S4 - Entrega

**C21** - Banco vazio: a partida cria a tabela `cartao`; uma segunda partida sobre a tabela existente não falha nem apaga dados (AC 17)
Proof: `./mvnw -q test -Dtest='PartidaTest#schemaCriadoNaPartidaEIdempotente'`

**C22** - A aplicação, rodando contra o MySQL do compose, atende o roteiro do desafio na porta 8080 (AC 17)
Proof: walk - `docker compose -f docker/docker-compose.yml up -d mysql`, `./mvnw spring-boot:run`, e o roteiro do `docs/desafio.md` por `curl` (criar, saldo, transações até `SALDO_INSUFICIENTE`, senha inválida, cartão inexistente)

**C23** - A conexão inicial espera até 60 s pelo MySQL antes de encerrar (AC 22)
Proof: `./mvnw -q test -Dtest='PartidaTest#esperaOMysqlPor60Segundos'`

**C24** - A declaração do serviço `mysql` no compose é idêntica à do desafio; só o `mongo` está comentado (AC 18)
Proof: `bash -c "diff <(sed -n '/^  mysql:/,\$p' docs/docker-compose.original.yml) <(sed -n '/^  mysql:/,\$p' docker/docker-compose.yml)"`
Proof: `bash -c "! rg -n '^  mongo:' docker/docker-compose.yml"`

**C25** - O README traz como rodar, como testar, as suposições, os riscos aceitos e o aviso do `mysql:5.7` amd64 (AC 19)
Proof: `bash -c "rg -q '^## Como rodar' README.md && rg -q '^## Como testar' README.md && rg -q '^## Suposições' README.md && rg -q '^## Riscos aceitos' README.md && rg -q 'DOCKER_DEFAULT_PLATFORM=linux/amd64' README.md"`

**C26** - O README lista cada linha de `## Assumptions` do plano (AC 19)
Proof: `bash -c "python3 -I .specs/features/mini-autorizador/assumptions_no_readme.py .specs/features/mini-autorizador/plan.md README.md"`

**C27** - A chave duplicada vira 422 só pelo erro de chave duplicada; outra violação de integridade não vira 422 (plan Assumptions, Inserção do cartão)
Proof: `./mvnw -q test -Dtest='TratamentoErrosTest#soChaveDuplicadaVira422'`

## Coverage

| Set (size) | Member -> proof | Unproven |
| --- | --- | --- |
| `POST /cartoes` statuses (3) | 201 C1 · 422 C3, C4 · 400 C19, C20 | - |
| `GET /cartoes/{numeroCartao}` statuses (2) | 200 C5 · 404 C6 | - |
| `POST /transacoes` statuses (5) | 201 `OK` C8 · 422 `SALDO_INSUFICIENTE` C9 · 422 `SENHA_INVALIDA` C10 · 422 `CARTAO_INEXISTENTE` C11 · 400 C18, C19, C20 | - |
| Landing doors (4) | door 1 tabela C21, C2 · door 2 débito atômico C13 · door 3 contrato C1, C5, C8, C22 · door 4 dependências C21, C22 (driver 8.0.x e Testcontainers 2.x rodam contra mysql:5.7) | - |
| ordem das regras, AC 12 (3) | cartão antes de senha C11 · senha antes de saldo C12 · tabela de pares C12 (`AutorizacaoTest`) | - |
| saldo, AC 8 e 9 (4 exemplos) | 500-10 C8 · 10-10 C8 · 0.00 com 0.01 C9 · 9.99 com 10.00 C9 | - |
| concorrência, AC 13 (2 exemplos) | 2×10 em 10 C13 · 60×10 em 500 C13 | - |
| valor inválido, AC 15 (6) | ausente C18 · 0 C18 · -10.00 C18 · 10.001 C18 · 18 inteiros C18 · 0.01 aceito C18 | - |
| número do cartão, AC 16 (6) | 12 C19 · 20 C19 · letra C19 · 13 C19 · 16 C19 · 19 C19 | - |
| senha, AC 16 (6) | ausente C20 · vazia C20 · 73 C20 · `ç` C20 · `1234` C20 · 72 ASCII C20 | - |
| startup config: schema e espera do Hikari (2 assemblies) | aplicação C22 · harness de teste C21, C23; os dois usam o mesmo `application.properties` | - |

## Swept

- validation: C18, C19, C20
- failure modes: C9, C10 (recusa não debita); C27 (erro inesperado não vira regra)
- idempotency: n/a - contrato sem id de transação; reenvio duplicado é risco aceito no README (C25)
- authorization: n/a - contrato sem autenticação; força bruta do PIN é risco aceito (C25)
- concurrency: C4, C13
- data lifecycle: C3 (cartão nunca sobrescrito); C21 (partida não apaga dados)
- dependency failure: C23
- state transitions: n/a - cartão não tem estados além do saldo
- observability: n/a - nenhum requisito de log ou métrica no desafio

## Handoff

- **Settled mid-build:** none
- **Dispatched:** ponytail-reviewer em `6405faf` (diff da árvore vazia até `6405faf`) - returned, 0 achados; impl-verifier em `0865207` - returned, 2 achados fechados em `7decbc6`
- **Boundary:** C1-C27 closed at `6405faf`
- **Abandoned:** none
- **Walked:** C22 - `docker compose -f docker/docker-compose.yml up -d mysql`, `./mvnw spring-boot:run`, roteiro por `curl`: criar 201 com eco, repetir 422, saldo `500.00`, 50 transações de `10.00` aprovadas e a 51ª `SALDO_INSUFICIENTE`, saldo `0.00`, senha errada `SENHA_INVALIDA`, cartão inexistente `CARTAO_INEXISTENTE`, saldo de inexistente 404. C23 - aplicação subida com o MySQL fora do ar, MySQL ligado 20 s depois: a partida concluiu 27 s após o início da conexão e o GET respondeu 404.
