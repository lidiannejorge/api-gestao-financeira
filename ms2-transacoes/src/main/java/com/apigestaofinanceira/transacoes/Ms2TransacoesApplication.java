package com.apigestaofinanceira.transacoes;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SecurityScheme(
		name = "basicAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "basic"
)

@SpringBootApplication
public class Ms2TransacoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ms2TransacoesApplication.class, args);
	}

}
