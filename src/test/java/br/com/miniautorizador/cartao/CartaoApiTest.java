package br.com.miniautorizador.cartao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.miniautorizador.IntegracaoBase;

class CartaoApiTest extends IntegracaoBase {

	@Test
	void criaCartaoNovoResponde201ComEcoNaOrdemDoContrato() {
		String numero = numeroNovo();

		HttpResponse<String> resposta = criarCartao(numero, "1234");

		assertThat(resposta.statusCode()).isEqualTo(201);
		assertThat(resposta.body()).isEqualTo("{\"senha\":\"1234\",\"numeroCartao\":\"" + numero + "\"}");
	}

	@Test
	void cartaoNasceComSaldo500() {
		String numero = numeroNovo();

		criarCartao(numero, "1234");

		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("500.00");
	}

	@Test
	void numeroRepetidoResponde422ENaoSobrescreve() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");
		assertThat(transacionar(numero, "1234", "100.00").statusCode()).isEqualTo(201);

		HttpResponse<String> repetido = criarCartao(numero, "9999");

		assertThat(repetido.statusCode()).isEqualTo(422);
		assertThat(repetido.body()).isEqualTo("{\"senha\":\"9999\",\"numeroCartao\":\"" + numero + "\"}");
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("400.00");
		assertThat(transacionar(numero, "9999", "1.00").body()).isEqualTo("SENHA_INVALIDA");
		assertThat(transacionar(numero, "1234", "1.00").statusCode()).isEqualTo(201);
	}

	@Test
	void doisPostsSimultaneosCriamUmCartaoSo() {
		String numero = numeroNovo();
		CountDownLatch largada = new CountDownLatch(1);

		List<CompletableFuture<Integer>> chamadas = List.of(1, 2).stream()
			.map(i -> CompletableFuture.supplyAsync(() -> {
				aguardar(largada);
				return criarCartao(numero, "1234").statusCode();
			}))
			.toList();
		largada.countDown();
		List<Integer> status = chamadas.stream().map(CompletableFuture::join).toList();

		assertThat(status).containsExactlyInAnyOrder(201, 422);
		assertThat(jdbc.sql("SELECT COUNT(*) FROM cartao WHERE numero = ?").param(numero).query(Integer.class).single())
			.isEqualTo(1);
	}

	@Test
	void saldoComDuasCasas() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		HttpResponse<String> inicial = get("/cartoes/" + numero);
		transacionar(numero, "1234", "4.85");
		HttpResponse<String> depois = get("/cartoes/" + numero);

		assertThat(inicial.statusCode()).isEqualTo(200);
		assertThat(inicial.headers().firstValue("Content-Type")).hasValueSatisfying(tipo -> assertThat(tipo).startsWith("application/json"));
		assertThat(inicial.body()).isEqualTo("500.00");
		assertThat(depois.body()).isEqualTo("495.15");
	}

	@Test
	void saldoDeCartaoInexistenteResponde404SemCorpo() {
		HttpResponse<String> inexistente = get("/cartoes/" + numeroNovo());
		HttpResponse<String> malformado = get("/cartoes/abc");
		String cadastrado = numeroNovo();
		criarCartao(cadastrado, "1234");
		HttpResponse<String> comEspaco = get("/cartoes/" + cadastrado + "%20");

		assertThat(inexistente.statusCode()).isEqualTo(404);
		assertThat(inexistente.body()).isEmpty();
		assertThat(malformado.statusCode()).isEqualTo(404);
		assertThat(malformado.body()).isEmpty();
		assertThat(comEspaco.statusCode()).isEqualTo(404);
		assertThat(comEspaco.body()).isEmpty();
	}

	@Test
	void senhaGuardadaSoComoHash() {
		String numero = numeroNovo();

		criarCartao(numero, "1234");

		String hash = jdbc.sql("SELECT senha_hash FROM cartao WHERE numero = ?").param(numero).query(String.class).single();
		assertThat(hash).startsWith("$2").doesNotContain("1234");
		assertThat(new BCryptPasswordEncoder().matches("1234", hash)).isTrue();
	}

	static void aguardar(CountDownLatch largada) {
		try {
			largada.await();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

}
