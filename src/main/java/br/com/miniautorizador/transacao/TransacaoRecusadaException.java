package br.com.miniautorizador.transacao;

class TransacaoRecusadaException extends RuntimeException {

	private final MotivoRecusa motivo;

	TransacaoRecusadaException(MotivoRecusa motivo) {
		super(motivo.name());
		this.motivo = motivo;
	}

	MotivoRecusa motivo() {
		return motivo;
	}

}
