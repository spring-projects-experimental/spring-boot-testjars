package com.example.springboottestcontainers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.experimental.boot.testjars.CommonsExecSpringBootServer;
import org.springframework.experimental.boot.testjars.SpringBootServerCommandLine;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers
public class TestSpringBootTestContainersApplication {

	@DynamicPropertySource
	static void springBootRunner(DynamicPropertyRegistry properties) {
		SpringBootServerCommandLine commandLine = SpringBootServerCommandLine.builder()
				.addClasspathEntries("/home/rwinch/code/rwinch/spring-authorization-server-sample/build/libs/spring-authorization-server-sample-0.0.1-SNAPSHOT.jar", "/home/rwinch/code/rwinch/spring-boot-testjars/src/main/resources/exported")
				.build();
		CommonsExecSpringBootServer runner = new CommonsExecSpringBootServer(commandLine);
		runner.start();
		properties.add("spring.security.oauth2.client.provider.spring.issuer-uri", () -> "http://localhost.example:" + runner.getApplicationPort());
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.from(SpringBootTestContainersApplication::main)
				.with(TestSpringBootTestContainersApplication.class)
				.run(args);
	}

}
