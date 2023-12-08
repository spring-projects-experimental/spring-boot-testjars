package com.example.springboottestcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

@SpringBootTest
class SpringBootTestContainersApplicationTests {

	@Bean
	@ServiceConnection
	MySQLContainer<?> mysqlContainer() {
		return new MySQLContainer<>(DockerImageName.parse("mysql:latest"));
	}

	@DynamicPropertySource
	static void authorizationServer(DynamicPropertyRegistry registry) throws IOException {
		SpringBootRunner runner = new SpringBootRunner();
		registry.add("spring.security.oauth2.client.provider.spring.issuer-uri", () -> "http://localhost:9000");
	}


	@Test
	void contextLoads() {
	}

}
