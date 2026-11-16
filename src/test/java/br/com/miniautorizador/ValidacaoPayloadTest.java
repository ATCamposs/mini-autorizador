package br.com.miniautorizador;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidacaoPayloadTest extends IntegracaoBase {

	@Test
	void numeroCartaoForaDoFormato() {
		for (String numero : new String[] { "123456789012", "12345678901234567890", "65498730256345a1" }) {
			assertThat(criarCartao(numero, "1234").statusCode()).as("POST /cartoes %s", numero).isEqualTo(400);
			assertThat(transacionar(numero, "1234", "1.00").statusCode()).as("POST /transacoes %s", numero).isEqualTo(400);
		}
		for (String numero : new String[] { aleatorio(13), aleatorio(16), aleatorio(19) }) {
			assertThat(criarCartao(numero, "1234").statusCode()).as("POST /cartoes %s", numero).isEqualTo(201);
			assertThat(transacionar(numero, "1234", "1.00").statusCode()).as("POST /transacoes %s", numero).isEqualTo(201);
		}
	}

	@Test
	void senhaForaDoFormato() {
		String existente = numeroNovo();
		criarCartao(existente, "1234");
		String semSenha = "{\"numeroCartao\":\"%s\"}";
		assertThat(post("/cartoes", semSenha.formatted(numeroNovo())).statusCode()).isEqualTo(400);
		assertThat(post("/transacoes", "{\"numeroCartao\":\"%s\",\"valor\":1.00}".formatted(existente)).statusCode())
			.isEqualTo(400);
		for (String senha : new String[] { "", "a".repeat(73), "senhaç" }) {
			assertThat(criarCartao(numeroNovo(), senha).statusCode()).as("POST /cartoes senha %s", senha).isEqualTo(400);
			assertThat(transacionar(existente, senha, "1.00").statusCode()).as("POST /transacoes senha %s", senha)
				.isEqualTo(400);
		}

		String longa = numeroNovo();
		assertThat(criarCartao(longa, "a".repeat(72)).statusCode()).isEqualTo(201);
		assertThat(transacionar(longa, "a".repeat(72), "1.00").statusCode()).isEqualTo(201);
		assertThat(transacionar(existente, "1234", "1.00").statusCode()).isEqualTo(201);
	}

	private static String aleatorio(int digitos) {
		return (numeroNovo() + numeroNovo()).substring(0, digitos);
	}

}
