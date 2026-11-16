# Executar o roteiro do desafio

Com a aplicação rodando ([Rodar a aplicação localmente](rodar-localmente.md)), os passos abaixo seguem a
ordem de avaliação do [desafio](../desafio.md).

1. Crie um cartão. Resposta esperada: `201` com `{"senha":"1234","numeroCartao":"6549873025634501"}`.

   ```bash
   curl -i -X POST localhost:8080/cartoes -H 'Content-Type: application/json' \
     -d '{"numeroCartao":"6549873025634501","senha":"1234"}'
   ```

2. Consulte o saldo. Resposta esperada: `200` com `500.00`.

   ```bash
   curl -i localhost:8080/cartoes/6549873025634501
   ```

3. Faça transações até o saldo acabar. Cada uma responde `201 OK`; com R$ 500,00, a 51ª de R$ 10,00
   responde `422 SALDO_INSUFICIENTE`.

   ```bash
   curl -i -X POST localhost:8080/transacoes -H 'Content-Type: application/json' \
     -d '{"numeroCartao":"6549873025634501","senhaCartao":"1234","valor":10.00}'
   ```

4. Use uma senha errada. Resposta esperada: `422 SENHA_INVALIDA`.

   ```bash
   curl -i -X POST localhost:8080/transacoes -H 'Content-Type: application/json' \
     -d '{"numeroCartao":"6549873025634501","senhaCartao":"0000","valor":1.00}'
   ```

5. Use um cartão que não existe. Resposta esperada: `422 CARTAO_INEXISTENTE`.

   ```bash
   curl -i -X POST localhost:8080/transacoes -H 'Content-Type: application/json' \
     -d '{"numeroCartao":"1111222233334444","senhaCartao":"1234","valor":1.00}'
   ```

Para repetir do zero, recrie o MySQL com `docker compose -f docker/docker-compose.yml down` e `up -d mysql`.
