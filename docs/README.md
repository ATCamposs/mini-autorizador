# Documentação

## How-to

- [Rodar a aplicação localmente](how-to/rodar-localmente.md)
- [Executar o roteiro do desafio](how-to/executar-o-roteiro-do-desafio.md)

## Referência

- [API](reference/api.md): rotas, status, corpos e formatos aceitos
- [Suposições](reference/suposicoes.md): as escolhas feitas onde o enunciado não decide

## Explicação

- [Sobre as regras de autorização](explanation/regras-de-autorizacao.md): sem `if`, ordem das regras, dinheiro
- [Riscos aceitos](explanation/riscos-aceitos.md)

## Decisões de arquitetura

- [ADR-0001: Debitar o saldo com UPDATE condicional atômico](adr/0001-debitar-o-saldo-com-update-condicional.md)
- [ADR-0002: Acessar o MySQL com Spring JDBC, sem JPA](adr/0002-acessar-o-mysql-com-spring-jdbc.md)

## Material do desafio

- [Enunciado](desafio.md)
- [Compose original](docker-compose.original.yml), guardado para conferir que o serviço `mysql` não mudou

Ainda não há tutorial: o projeto não tem nada que precise ser ensinado passo a passo além dos how-tos.
