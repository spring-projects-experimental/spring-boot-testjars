package example.oauth2.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.experimental.boot.testjars.CommonsExecSpringBootServer;
import org.springframework.experimental.boot.testjars.SpringBootServerCommandLine;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers
class TestOauth2LoginMain {

	@DynamicPropertySource
	static void springBootRunner(DynamicPropertyRegistry properties) {
		SpringBootServerCommandLine commandLine = SpringBootServerCommandLine.builder()
				// FIXME: copy spring.factories to temp folder and auto add to classpath
				.addClasspathEntries("/home/rwinch/code/rwinch/spring-boot-testjars/samples/authorization-server/build/libs/authorization-server-0.0.1-SNAPSHOT.jar", "/home/rwinch/code/rwinch/spring-boot-testjars/spring-boot-testjars/src/main/resources/exported")
				.build();
		CommonsExecSpringBootServer runner = new CommonsExecSpringBootServer(commandLine);
		runner.start();
		properties.add("spring.security.oauth2.client.provider.spring.issuer-uri", () -> "http://127.0.0.1:" + runner.getApplicationPort());
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.from(Oauth2LoginMain::main)
				.with(TestOauth2LoginMain.class)
				.run(args);
	}

}