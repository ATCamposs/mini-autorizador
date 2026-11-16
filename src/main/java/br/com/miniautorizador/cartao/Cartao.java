package br.com.miniautorizador.cartao;

import java.math.BigDecimal;

public record Cartao(String numero, String senhaHash, BigDecimal saldo) {

	/** ISO/IEC 7812. O GET também filtra por ele: o MySQL 5.7 ignora espaço à direita ao comparar VARCHAR. */
	public static final String FORMATO_NUMERO = "\\d{13,19}";

	public static final String FORMATO_SENHA = "[\\x20-\\x7E]{1,72}";

}
