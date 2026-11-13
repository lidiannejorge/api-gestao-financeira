package com.apigestaofinanceira.usuarios;

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
public class Ms1UsuarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ms1UsuarioApplication.class, args);
	}

}
