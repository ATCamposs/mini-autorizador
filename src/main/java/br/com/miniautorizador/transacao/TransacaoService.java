package br.com.miniautorizador.transacao;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.miniautorizador.cartao.Cartao;
import br.com.miniautorizador.cartao.CartaoRepository;

/** Regras de autorização sem if: cada regra recusa por exceção, na ordem cartão, senha, saldo. */
@Service
public class TransacaoService {

	private final CartaoRepository cartoes;

	private final PasswordEncoder encoder;

	TransacaoService(CartaoRepository cartoes, PasswordEncoder encoder) {
		this.cartoes = cartoes;
		this.encoder = encoder;
	}

	void autorizar(String numeroCartao, String senha, BigDecimal valor) {
		Cartao cartao = cartoes.buscar(numeroCartao).orElseThrow(() -> recusa(MotivoRecusa.CARTAO_INEXISTENTE));
		Optional.of(cartao)
			.filter(c -> encoder.matches(senha, c.senhaHash()))
			.orElseThrow(() -> recusa(MotivoRecusa.SENHA_INVALIDA));
		// O UPDATE condicional é a checagem de saldo: entre instâncias, só o banco decide.
		Optional.of(cartoes.debitar(numeroCartao, valor))
			.filter(debitou -> debitou == 1)
			.orElseThrow(() -> recusa(MotivoRecusa.SALDO_INSUFICIENTE));
	}

	private static TransacaoRecusadaException recusa(MotivoRecusa motivo) {
		return new TransacaoRecusadaException(motivo);
	}

}
