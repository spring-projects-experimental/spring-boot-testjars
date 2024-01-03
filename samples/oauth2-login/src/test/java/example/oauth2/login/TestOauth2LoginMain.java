/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.oauth2.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.experimental.boot.testjars.CommonsExecWebServer;
import org.springframework.experimental.boot.testjars.WebServerCommandLine;
import org.springframework.test.context.DynamicPropertyRegistry;

@TestConfiguration(proxyBeanMethods = false)
class TestOauth2LoginMain {

	@Bean
	static CommonsExecWebServer springBootRunner(DynamicPropertyRegistry properties) {
		// FIXME: Return WebServerCommandLine and add BeanDefinitionRegistryPostProcessor which:
		//  - finds all WebServerCommandLine
		//  - creates CommonsExecWebServer from the WebServerCommandLine
		//  - Maps the port to a property based upon an annotation
		//  - Supports meta annotations so AuthZ Server can have an annotation that maps "spring.security.oauth2.client.provider.spring.issuer-uri", () -> "http://127.0.0.1:" + runner.getPort()
		WebServerCommandLine commandLine = WebServerCommandLine.builder()
				// FIXME: copy spring.factories to temp folder and auto add to classpath
				.classpath(cp -> cp
					.files("/home/rwinch/code/rwinch/spring-boot-testjars/samples/authorization-server/build/libs/authorization-server-0.0.1-SNAPSHOT.jar")
				)
				.build();
		CommonsExecWebServer runner = new CommonsExecWebServer(commandLine);
		runner.start();
		properties.add("spring.security.oauth2.client.provider.spring.issuer-uri", () -> "http://127.0.0.1:" + runner.getPort());
		return runner;
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.from(Oauth2LoginMain::main)
				.with(TestOauth2LoginMain.class)
				.run(args);
	}

}