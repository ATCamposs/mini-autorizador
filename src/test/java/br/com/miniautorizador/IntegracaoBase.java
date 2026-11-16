package br.com.miniautorizador;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Sobe a aplicação inteira contra o mysql:5.7 do Testcontainers e fala HTTP de verdade. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegracaoBase {

	private static final HttpClient HTTP = HttpClient.newHttpClient();

	@LocalServerPort
	private int porta;

	@Autowired
	protected JdbcClient jdbc;

	protected static String numeroNovo() {
		return String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L, 9_999_999_999_999_999L));
	}

	protected HttpResponse<String> post(String caminho, String json) {
		return post(caminho, json, "*/*");
	}

	protected HttpResponse<String> post(String caminho, String json, String accept) {
		return enviar(HttpRequest.newBuilder(uri(caminho))
			.header("Content-Type", "application/json")
			.header("Accept", accept)
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build());
	}

	protected HttpResponse<String> get(String caminho) {
		return enviar(HttpRequest.newBuilder(uri(caminho)).GET().build());
	}

	protected HttpResponse<String> criarCartao(String numero, String senha) {
		return post("/cartoes", "{\"numeroCartao\":\"%s\",\"senha\":\"%s\"}".formatted(numero, senha));
	}

	protected HttpResponse<String> transacionar(String numero, String senha, String valor) {
		return post("/transacoes",
				"{\"numeroCartao\":\"%s\",\"senhaCartao\":\"%s\",\"valor\":%s}".formatted(numero, senha, valor));
	}

	protected BigDecimal saldoNoBanco(String numero) {
		return jdbc.sql("SELECT saldo FROM cartao WHERE numero = ?").param(numero).query(BigDecimal.class).single();
	}

	private URI uri(String caminho) {
		return URI.create("http://localhost:" + porta + caminho);
	}

	private static HttpResponse<String> enviar(HttpRequest requisicao) {
		try {
			return HTTP.send(requisicao, HttpResponse.BodyHandlers.ofString());
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
