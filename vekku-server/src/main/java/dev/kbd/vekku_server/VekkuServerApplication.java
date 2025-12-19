package dev.kbd.vekku_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VekkuServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VekkuServerApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.web.client.RestClient.Builder restClientBuilder() {
		return org.springframework.web.client.RestClient.builder();
	}

}
