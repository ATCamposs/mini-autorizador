package br.com.miniautorizador.transacao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.miniautorizador.cartao.Cartao;
import br.com.miniautorizador.cartao.CartaoRepository;

/** Ordem das regras: cartão existe, depois senha, depois saldo; vence a primeira que barra. */
class AutorizacaoTest {

	private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

	private static final String NUMERO = "6549873025634501";

	@ParameterizedTest(name = "existe={0} senhaCerta={1} temSaldo={2} -> {3}")
	@CsvSource({
		"false, false, false, CARTAO_INEXISTENTE",
		"false, true,  false, CARTAO_INEXISTENTE",
		"false, false, true,  CARTAO_INEXISTENTE",
		"false, true,  true,  CARTAO_INEXISTENTE",
		"true,  false, false, SENHA_INVALIDA",
		"true,  false, true,  SENHA_INVALIDA",
		"true,  true,  false, SALDO_INSUFICIENTE",
		"true,  true,  true,  APROVADA",
	})
	void primeiraRegraQueBarraVence(boolean existe, boolean senhaCerta, boolean temSaldo, String esperado) {
		CartaoFalso cartoes = new CartaoFalso();
		if (existe) {
			String saldo = temSaldo ? "100.00" : "5.00";
			cartoes.porNumero.put(NUMERO, new Cartao(NUMERO, ENCODER.encode("1234"), new BigDecimal(saldo)));
		}
		TransacaoService service = new TransacaoService(cartoes, ENCODER);
		String senha = senhaCerta ? "1234" : "errada";

		Runnable autorizar = () -> service.autorizar(NUMERO, senha, new BigDecimal("10.00"));

		if (esperado.equals("APROVADA")) {
			assertThatCode(autorizar::run).doesNotThrowAnyException();
		}
		else {
			assertThatThrownBy(autorizar::run).isInstanceOf(TransacaoRecusadaException.class).hasMessage(esperado);
		}
	}

	/** Repositório em memória com a mesma regra do UPDATE condicional. */
	static class CartaoFalso extends CartaoRepository {

		final Map<String, Cartao> porNumero = new HashMap<>();

		CartaoFalso() {
			super(null);
		}

		@Override
		public Optional<Cartao> buscar(String numero) {
			return Optional.ofNullable(porNumero.get(numero));
		}

		@Override
		public int debitar(String numero, BigDecimal valor) {
			return buscar(numero).filter(c -> c.saldo().compareTo(valor) >= 0).map(c -> 1).orElse(0);
		}

	}

}
