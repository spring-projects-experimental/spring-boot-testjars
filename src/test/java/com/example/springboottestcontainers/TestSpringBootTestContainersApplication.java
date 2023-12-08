package com.example.springboottestcontainers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers
public class TestSpringBootTestContainersApplication {

	@DynamicPropertySource
	static void springBootRunner(DynamicPropertyRegistry properties) {
		SpringBootRunner runner = new SpringBootRunner();
		runner.start();
		properties.add("spring.security.oauth2.client.provider.spring.issuer-uri", () -> runner.getUrl());
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.from(SpringBootTestContainersApplication::main).with(TestSpringBootTestContainersApplication.class).run(args);
	}

}
