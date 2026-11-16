# Revisão Ponytail: mini-autorizador

HEAD: `6405faf`. Intervalo: `4b825dc642cb6eb9a060e54bf8d69288fbee4904..6405faf` (árvore vazia até o HEAD).

O que a mudança faz: cria do zero uma API Spring Boot com três rotas (`POST /cartoes`, `GET /cartoes/{numeroCartao}`, `POST /transacoes`) sobre uma tabela `cartao` no MySQL 5.7 do compose. A senha vai só como hash BCrypt, o débito é um `UPDATE` condicional que o banco decide sozinho (vale entre instâncias), e as regras de autorização encadeiam `Optional` e exceções sem `if`, na ordem cartão, senha, saldo.

Parece bom. Nenhum achado de Must fix, Should fix ou Nice to have.

O que conferi: todo o código de `src/main`, `src/test`, `pom.xml`, `application.properties`, `schema.sql` e o `README.md`, contra o contrato do `docs/desafio.md` e as decisões do `plan.md`. Carga assumida: mais de uma instância sobre o mesmo MySQL (regra da casa), e o próprio plano pede isso. Pontos olhados um a um, sem defeito achado:

- Débito: `UPDATE ... WHERE numero = ? AND saldo >= ?` em auto-commit; com `valor > 0` garantido pelo `@Positive`, a linha casada sempre muda, então "1 linha" é aprovação mesmo com o padrão do Connector/J de contar linhas encontradas.
- Cartão duplicado ao mesmo tempo: a chave primária barra o segundo; o erro 1062 vira `DuplicateKeyException` e só ele vira 422; `NOT NULL` violado continua 500 (`TratamentoErrosTest`).
- Ordem das regras e "primeira que barra vence": `TransacaoService.java:25-34`, coberta pelas 8 combinações do `AutorizacaoTest`.
- Validação: `\d` do Java só casa dígito ASCII; senha limitada a 72 caracteres ASCII, abaixo do limite do BCrypt; `@Digits(17,2)` cabe em `DECIMAL(19,2)`.
- Dinheiro só em `BigDecimal`/`DECIMAL(19,2)`; o saldo sai como `500.00` e `495.15`.
- `Accept: application/json` em `/transacoes` não dá 406 (o conversor de `String` aceita qualquer tipo).
- Partida: `CREATE TABLE IF NOT EXISTS` é idempotente com duas instâncias; `initialization-fail-timeout=60000` faz o Hikari tentar por 60 s.
- Suíte rodada uma vez no `6405faf` (árvore limpa fora do `checks.md`): 30 testes, 0 falhas, incluindo 60 transações simultâneas contra o `mysql:5.7` real.

Veredito: pode subir.

Não conferido: o teste de partida (`PartidaTest.esperaOMysqlPor60Segundos`) só lê a configuração; não subi a aplicação com o MySQL ainda fora do ar para ver a espera de 60 s acontecer.
