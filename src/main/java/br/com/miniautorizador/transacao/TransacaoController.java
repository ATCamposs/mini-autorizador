package br.com.miniautorizador.transacao;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TransacaoController {

	private final TransacaoService service;

	TransacaoController(TransacaoService service) {
		this.service = service;
	}

	@PostMapping("/transacoes")
	ResponseEntity<String> transacionar(@Valid @RequestBody NovaTransacao transacao) {
		service.autorizar(transacao.numeroCartao(), transacao.senhaCartao(), transacao.valor());
		return ResponseEntity.status(HttpStatus.CREATED).body("OK");
	}

	@ExceptionHandler
	ResponseEntity<String> recusada(TransacaoRecusadaException e) {
		return ResponseEntity.status(422).body(e.motivo().name());
	}

	record NovaTransacao(
			@NotNull @Pattern(regexp = "\\d{13,19}") String numeroCartao,
			@NotNull @Pattern(regexp = "[\\x20-\\x7E]{1,72}") String senhaCartao,
			@NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal valor) {
	}

}
