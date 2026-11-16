package br.com.miniautorizador.cartao;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartoes")
class CartaoController {

	private final CartaoService service;

	CartaoController(CartaoService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<NovoCartao> criar(@Valid @RequestBody NovoCartao novo) {
		try {
			service.criar(novo.numeroCartao(), novo.senha());
			return ResponseEntity.status(HttpStatus.CREATED).body(novo);
		}
		catch (CartaoJaExisteException e) {
			return ResponseEntity.status(422).body(novo);
		}
	}

	@GetMapping("/{numeroCartao}")
	ResponseEntity<BigDecimal> saldo(@PathVariable String numeroCartao) {
		return service.saldo(numeroCartao).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** A ordem dos componentes é a ordem do JSON no contrato: senha antes do número. */
	record NovoCartao(
			@NotNull @Pattern(regexp = Cartao.FORMATO_SENHA) String senha,
			@NotNull @Pattern(regexp = Cartao.FORMATO_NUMERO) String numeroCartao) {
	}

}
