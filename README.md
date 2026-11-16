# Mini autorizador

API REST em Spring Boot que cria cartões de benefício, consulta saldo e autoriza transações, conforme o
[desafio da VR Benefícios](docs/desafio.md). As transações não são persistidas; o cartão e o saldo, sim.

## Como rodar

Requisitos: JDK 17 ou mais novo e Docker.

```bash
docker compose -f docker/docker-compose.yml up -d mysql
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. A tabela é criada na partida. Se o MySQL ainda estiver
inicializando, a aplicação espera até 60 s pela primeira conexão.

**Mac com Apple Silicon:** a imagem `mysql:5.7` só existe para amd64. Rode com emulação:

```bash
DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose -f docker/docker-compose.yml up -d mysql
```

## Como testar

```bash
./mvnw test
```

Os testes de integração sobem um `mysql:5.7` pelo Testcontainers (precisa de Docker) e falam HTTP com a
aplicação de verdade. O roteiro do desafio, à mão:

```bash
curl -i -X POST localhost:8080/cartoes -H 'Content-Type: application/json' \
  -d '{"numeroCartao":"6549873025634501","senha":"1234"}'
curl -i localhost:8080/cartoes/6549873025634501
curl -i -X POST localhost:8080/transacoes -H 'Content-Type: application/json' \
  -d '{"numeroCartao":"6549873025634501","senhaCartao":"1234","valor":10.00}'
```

## Contrato

| Rota | Sucesso | Recusa |
| --- | --- | --- |
| `POST /cartoes` `{numeroCartao, senha}` | 201, ecoa o JSON recebido; saldo inicial `500.00` | 422 com o JSON recebido se o número já existe; 400 se o payload é inválido |
| `GET /cartoes/{numeroCartao}` | 200, saldo como número JSON (`495.15`) | 404 sem corpo |
| `POST /transacoes` `{numeroCartao, senhaCartao, valor}` | 201, corpo `OK` | 422 com `CARTAO_INEXISTENTE`, `SENHA_INVALIDA` ou `SALDO_INSUFICIENTE`; 400 se o payload é inválido |

Formatos aceitos: `numeroCartao` com 13 a 19 dígitos; senha com 1 a 72 caracteres ASCII imprimíveis;
`valor` positivo, com até 2 casas decimais e até 17 dígitos inteiros.

## Decisões

**Concorrência entre instâncias.** O débito é um único comando no banco:

```sql
UPDATE cartao SET saldo = saldo - :valor WHERE numero = :numero AND saldo >= :valor
```

A transação só é aprovada se uma linha mudou. O InnoDB trava a linha durante o `UPDATE` e reavalia a
condição depois que a concorrente termina, então duas transações de R$ 10,00 num cartão com R$ 10,00,
em instâncias diferentes, terminam com uma aprovada, uma `SALDO_INSUFICIENTE` e saldo `0.00`. Não há lock
segurado entre ler e gravar, nem retry. O teste `ConcorrenciaTest` dispara 60 transações simultâneas
contra um saldo de R$ 500,00 e exige exatamente 50 aprovadas.

**Regras sem `if`.** `TransacaoService` encadeia as três regras com `Optional` e exceções de domínio, na
ordem do desafio: cartão existe, senha confere, saldo suficiente. A primeira que barra decide a resposta.
O resto do código usa estruturas normais.

**Dinheiro.** `BigDecimal` no Java e `DECIMAL(19,2)` no MySQL, os dois exatos. Nada de `double` ou
`float`; valor com mais de 2 casas é recusado, nunca arredondado.

**Spring JDBC no lugar de JPA.** O Hibernate 6 e 7 só suporta MySQL 8.0 ou mais novo (aviso HHH000511), e
o compose do desafio usa o 5.7. Com uma tabela só, SQL escrito à mão com `JdbcClient` é mais simples e
deixa o `INSERT` e o `UPDATE` condicional explícitos.

**Driver fixado em 8.0.33.** As linhas 8.4, 9.x e 26.x do Connector/J só suportam MySQL 8.0 ou mais novo.

## Suposições

| Suposição | Escolha |
| --- | --- |
| Stack | Java 17, Spring Boot 4.1, Maven wrapper |
| Banco | MySQL 5.7 do compose; o serviço Mongo ficou comentado |
| Concorrência entre instâncias | `UPDATE` condicional atômico (ver Decisões) |
| Desafio sem `if` | aplicado às regras de autorização |
| Ordem das regras | cartão existe → senha → saldo |
| Dinheiro | `BigDecimal` com 2 casas, `DECIMAL(19,2)`, sem arredondamento |
| Content-Type das respostas de texto | `text/plain` por padrão; nunca 406, mesmo com `Accept: application/json` |
| Saldo igual ao valor | aprova e zera o saldo |
| Idioma | código, commits e documentação em português |
| Senha | guardada só como hash BCrypt |
| Payload malformado | 400, separado do 422 das regras de autorização |
| Formato do número | 13 a 19 dígitos (ISO/IEC 7812) |
| Testes | unidade nas regras; integração e concorrência com Testcontainers e `mysql:5.7` |
| Organização | pacotes por feature (`cartao`, `transacao`) |
| Schema | `schema.sql` executado na partida, idempotente |
| Cartão duplicado concorrente | a chave primária barra o segundo, que recebe 422 |
| Inserção do cartão | `INSERT` explícito; só chave duplicada vira 422 |
| Formato da senha | 1 a 72 caracteres ASCII imprimíveis (limite do BCrypt) |
| Partida sem MySQL pronto | espera até 60 s pela primeira conexão |
| Driver MySQL | Connector/J 8.0.33 |
| Teto do valor | até 17 dígitos inteiros |
| Erro inesperado | 500 com o corpo padrão do Spring |
| GET com número malformado | 404, como cartão inexistente |
| Riscos aceitos | ver a seção abaixo |
| Transação de banco | sem transação explícita: o débito é um `UPDATE` em auto-commit |
| GET com Accept sem JSON | o saldo só é servido como JSON |
| Acesso a dados | Spring JDBC (`JdbcClient`) |
| Execução | `./mvnw spring-boot:run` contra o compose, sem imagem da aplicação |
| Compose | só o serviço `mongo` comentado; o `mysql` está como veio |
| GitHub | repositório `mini-autorizador`, com o histórico de commits |

## Riscos aceitos

- **Reenvio duplica o débito.** O contrato não tem id de transação, então um retry depois de timeout é
  uma transação nova. Como resolveria: id de idempotência no payload, gravado com chave única junto do
  débito.
- **Força bruta da senha.** Uma senha de 4 dígitos tem 10 mil combinações e não há bloqueio. Como
  resolveria: contador de tentativas por cartão com bloqueio temporário, e limite de requisições por
  origem.
- **Número do cartão em claro.** O contrato exige ecoar o número, e ele é a chave da tabela. Como
  resolveria: tokenização do número, com o cofre fora da aplicação, no escopo PCI.
