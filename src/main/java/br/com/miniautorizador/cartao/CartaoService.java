package br.com.miniautorizador.cartao;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CartaoService {

	static final BigDecimal SALDO_INICIAL = new BigDecimal("500.00");

	private final CartaoRepository repository;

	private final PasswordEncoder encoder;

	CartaoService(CartaoRepository repository, PasswordEncoder encoder) {
		this.repository = repository;
		this.encoder = encoder;
	}

	void criar(String numero, String senha) {
		try {
			repository.inserir(new Cartao(numero, encoder.encode(senha), SALDO_INICIAL));
		}
		catch (DuplicateKeyException e) {
			throw new CartaoJaExisteException(e);
		}
	}

	Optional<BigDecimal> saldo(String numero) {
		return repository.buscar(numero).map(Cartao::saldo);
	}

}
