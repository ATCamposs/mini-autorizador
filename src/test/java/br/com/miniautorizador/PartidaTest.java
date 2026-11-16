package br.com.miniautorizador;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

class PartidaTest extends IntegracaoBase {

	@Autowired
	private DataSource dataSource;

	@Test
	void schemaCriadoNaPartidaEIdempotente() {
		String numero = numeroNovo();
		assertThat(criarCartao(numero, "1234").statusCode()).isEqualTo(201);

		new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);

		assertThat(saldoNoBanco(numero)).isEqualByComparingTo("500.00");
	}

	@Test
	void esperaOMysqlPor60Segundos() {
		assertThat(dataSource).isInstanceOf(HikariDataSource.class);
		assertThat(((HikariDataSource) dataSource).getInitializationFailTimeout()).isEqualTo(60_000L);
	}

}
