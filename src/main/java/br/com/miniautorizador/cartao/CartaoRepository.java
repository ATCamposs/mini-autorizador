package br.com.miniautorizador.cartao;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CartaoRepository {

	private final JdbcClient jdbc;

	public CartaoRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** Lança DuplicateKeyException quando o número já existe: a chave primária decide, inclusive entre instâncias. */
	void inserir(Cartao cartao) {
		jdbc.sql("INSERT INTO cartao (numero, senha_hash, saldo) VALUES (?, ?, ?)")
			.params(cartao.numero(), cartao.senhaHash(), cartao.saldo())
			.update();
	}

	public Optional<Cartao> buscar(String numero) {
		return jdbc.sql("SELECT numero, senha_hash, saldo FROM cartao WHERE numero = ?")
			.param(numero)
			.query((rs, i) -> new Cartao(rs.getString("numero"), rs.getString("senha_hash"), rs.getBigDecimal("saldo")))
			.optional();
	}

	/** Débito atômico: o banco só debita se houver saldo. Devolve 1 se debitou, 0 se não. */
	public int debitar(String numero, BigDecimal valor) {
		return jdbc.sql("UPDATE cartao SET saldo = saldo - ? WHERE numero = ? AND saldo >= ?")
			.params(valor, numero, valor)
			.update();
	}

}
