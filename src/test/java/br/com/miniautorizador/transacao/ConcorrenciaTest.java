package br.com.miniautorizador.transacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import br.com.miniautorizador.IntegracaoBase;

/** Várias requisições ao mesmo tempo no mesmo cartão, cada uma com a própria conexão: o UPDATE condicional decide. */
class ConcorrenciaTest extends IntegracaoBase {

	@Test
	void duasDeDezComSaldoDezAprovamUmaSo() throws Exception {
		String numero = numeroNovo();
		criarCartao(numero, "1234");
		jdbc.sql("UPDATE cartao SET saldo = 10.00 WHERE numero = ?").param(numero).update();

		List<Integer> status = simultaneas(2, numero, "10.00");

		assertThat(status).containsExactlyInAnyOrder(201, 422);
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("0.00");
	}

	@Test
	void sessentaDeDezComSaldoQuinhentosAprovamCinquenta() throws Exception {
		String numero = numeroNovo();
		criarCartao(numero, "1234");

		List<Integer> status = simultaneas(60, numero, "10.00");

		assertThat(status).filteredOn(s -> s == 201).hasSize(50);
		assertThat(status).filteredOn(s -> s == 422).hasSize(10);
		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("0.00");
	}

	private List<Integer> simultaneas(int quantas, String numero, String valor) throws Exception {
		CountDownLatch largada = new CountDownLatch(1);
		ExecutorService threads = Executors.newFixedThreadPool(quantas);
		try {
			List<Future<Integer>> futuros = IntStream.range(0, quantas)
				.mapToObj(i -> threads.submit((Callable<Integer>) () -> {
					largada.await();
					return transacionar(numero, "1234", valor).statusCode();
				}))
				.toList();
			largada.countDown();
			return futuros.stream().map(ConcorrenciaTest::resultado).toList();
		}
		finally {
			threads.shutdownNow();
		}
	}

	private static Integer resultado(Future<Integer> futuro) {
		try {
			return futuro.get();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
