package br.com.miniautorizador.transacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import br.com.miniautorizador.IntegracaoBase;

class TransacaoApiTest extends IntegracaoBase {

	@Test
	void transacaoAprovadaDebitaEResponde201Ok() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		HttpResponse<String> aprovada = transacionar(numero, "1234", "10.00");

		assertThat(aprovada.statusCode()).isEqualTo(201);
		assertThat(aprovada.body()).isEqualTo("OK");
		assertThat(aprovada.headers().firstValue("Content-Type")).hasValueSatisfying(tipo -> assertThat(tipo).startsWith("text/plain"));
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("490.00");

		jdbc.sql("UPDATE cartao SET saldo = 10.00 WHERE numero = ?").param(numero).update();
		assertThat(transacionar(numero, "1234", "10.00").statusCode()).isEqualTo(201);
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("0.00");
	}

	@Test
	void saldoInsuficienteResponde422SemDebitar() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		jdbc.sql("UPDATE cartao SET saldo = 0.00 WHERE numero = ?").param(numero).update();
		HttpResponse<String> semSaldo = transacionar(numero, "1234", "0.01");
		assertThat(semSaldo.statusCode()).isEqualTo(422);
		assertThat(semSaldo.body()).isEqualTo("SALDO_INSUFICIENTE");
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("0.00");

		jdbc.sql("UPDATE cartao SET saldo = 9.99 WHERE numero = ?").param(numero).update();
		HttpResponse<String> quaseSaldo = transacionar(numero, "1234", "10.00");
		assertThat(quaseSaldo.statusCode()).isEqualTo(422);
		assertThat(quaseSaldo.body()).isEqualTo("SALDO_INSUFICIENTE");
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("9.99");
	}

	@Test
	void senhaInvalidaResponde422SemDebitar() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		HttpResponse<String> resposta = transacionar(numero, "4321", "10.00");

		assertThat(resposta.statusCode()).isEqualTo(422);
		assertThat(resposta.body()).isEqualTo("SENHA_INVALIDA");
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("500.00");
	}

	@Test
	void cartaoInexistenteResponde422() {
		HttpResponse<String> resposta = transacionar(numeroNovo(), "qualquer", "10.00");

		assertThat(resposta.statusCode()).isEqualTo(422);
		assertThat(resposta.body()).isEqualTo("CARTAO_INEXISTENTE");
	}

	@Test
	void senhaErradaComSaldoInsuficienteRespondeSenhaInvalida() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		HttpResponse<String> resposta = transacionar(numero, "errada", "600.00");

		assertThat(resposta.statusCode()).isEqualTo(422);
		assertThat(resposta.body()).isEqualTo("SENHA_INVALIDA");
	}

	@Test
	void acceptJsonNaoDa406() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");
		String json = "{\"numeroCartao\":\"%s\",\"senhaCartao\":\"%s\",\"valor\":%s}";

		HttpResponse<String> aprovada = post("/transacoes", json.formatted(numero, "1234", "1.00"), "application/json");
		HttpResponse<String> recusada = post("/transacoes", json.formatted(numero, "errada", "1.00"), "application/json");

		assertThat(aprovada.statusCode()).isEqualTo(201);
		assertThat(aprovada.body()).isEqualTo("OK");
		assertThat(recusada.statusCode()).isEqualTo(422);
		assertThat(recusada.body()).isEqualTo("SENHA_INVALIDA");
	}

	@Test
	void dezCentavosSomamExato() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		for (int i = 0; i < 10; i++) {
			assertThat(transacionar(numero, "1234", "0.10").statusCode()).isEqualTo(201);
		}

		assertThat(get("/cartoes/" + numero).body()).isEqualTo("499.00");
	}

	@Test
	void valorInvalidoResponde400() {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		assertThat(post("/transacoes", "{\"numeroCartao\":\"%s\",\"senhaCartao\":\"1234\"}".formatted(numero)).statusCode())
			.isEqualTo(400);
		for (String valor : new String[] { "0", "-10.00", "10.001", "100000000000000000.00" }) {
			assertThat(transacionar(numero, "1234", valor).statusCode()).as(valor).isEqualTo(400);
		}
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("500.00");

		assertThat(transacionar(numero, "1234", "0.01").statusCode()).isEqualTo(201);
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("499.99");
	}

}
