# Rodar a aplicação localmente

Requisitos: JDK 17 ou mais novo e Docker.

1. Suba o MySQL do desafio:

   ```bash
   docker compose -f docker/docker-compose.yml up -d mysql
   ```

   Em Mac com Apple Silicon, a imagem `mysql:5.7` só existe para amd64. Rode com emulação:

   ```bash
   DOCKER_DEFAULT_PLATFORM=linux/amd64 docker compose -f docker/docker-compose.yml up -d mysql
   ```

2. Suba a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   A tabela `cartao` é criada na partida. Se o MySQL ainda estiver inicializando, a aplicação espera
   até 60 s pela primeira conexão.

## Como saber que subiu

O log mostra `Started MiniAutorizadorApplication`, e a consulta de um cartão que não existe responde 404:

```bash
curl -i localhost:8080/cartoes/1234567890123
```

## Como desfazer

Pare a aplicação com Ctrl+C e derrube o MySQL:

```bash
docker compose -f docker/docker-compose.yml down
```
