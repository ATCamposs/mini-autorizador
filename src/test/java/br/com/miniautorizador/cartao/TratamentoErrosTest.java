package br.com.miniautorizador.cartao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.miniautorizador.IntegracaoBase;

class TratamentoErrosTest extends IntegracaoBase {

	@Autowired
	private CartaoRepository repository;

	@Test
	void soChaveDuplicadaVira422() {
		String numero = numeroNovo();
		Cartao cartao = new Cartao(numero, "hash", new BigDecimal("500.00"));
		repository.inserir(cartao);
		assertThatThrownBy(() -> repository.inserir(cartao)).isInstanceOf(DuplicateKeyException.class);

		PasswordEncoder hashNulo = new PasswordEncoder() {
			public String encode(CharSequence senha) {
				return null;
			}

			public boolean matches(CharSequence senha, String hash) {
				return false;
			}
		};
		CartaoService service = new CartaoService(repository, hashNulo);
		assertThatThrownBy(() -> service.criar(numeroNovo(), "1234"))
			.isInstanceOf(DataIntegrityViolationException.class)
			.isNotInstanceOf(DuplicateKeyException.class)
			.isNotInstanceOf(CartaoJaExisteException.class);
	}

}
