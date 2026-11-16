package br.com.miniautorizador.cartao;

class CartaoJaExisteException extends RuntimeException {

	CartaoJaExisteException(Throwable causa) {
		super("cartão já existe", causa);
	}

}
