package br.com.miniautorizador.cartao;

import java.math.BigDecimal;

public record Cartao(String numero, String senhaHash, BigDecimal saldo) {
}
