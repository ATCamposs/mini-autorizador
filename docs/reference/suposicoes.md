# Suposições

O desafio pede que as suposições fiquem documentadas. Cada linha é uma escolha feita onde o enunciado
não decide. O porquê das principais está nas [ADRs](../adr/) e em [explicações](../explanation/).

| Suposição | Escolha |
| --- | --- |
| Stack | Java 17, Spring Boot 4.1, Maven wrapper |
| Banco | MySQL 5.7 do compose; o serviço Mongo ficou comentado |
| Concorrência entre instâncias | `UPDATE` condicional atômico ([ADR-0001](../adr/0001-debitar-o-saldo-com-update-condicional.md)) |
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
| Driver MySQL | Connector/J 8.0.33, a última linha que suporta MySQL 5.7 |
| Teto do valor | até 17 dígitos inteiros |
| Erro inesperado | 500 com o corpo padrão do Spring |
| GET com número malformado | 404, como cartão inexistente |
| Riscos aceitos | ver [Riscos aceitos](../explanation/riscos-aceitos.md) |
| Transação de banco | sem transação explícita: o débito é um `UPDATE` em auto-commit |
| GET com Accept sem JSON | o saldo só é servido como JSON |
| Acesso a dados | Spring JDBC (`JdbcClient`) ([ADR-0002](../adr/0002-acessar-o-mysql-com-spring-jdbc.md)) |
| Execução | `./mvnw spring-boot:run` contra o compose, sem imagem da aplicação |
| Compose | só o serviço `mongo` comentado; o `mysql` está como veio |
| GitHub | repositório `mini-autorizador`, com o histórico de commits |
