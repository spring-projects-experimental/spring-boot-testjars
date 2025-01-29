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
import org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean;
import org.springframework.experimental.boot.test.context.OAuth2ClientProviderIssuerUri;
import testjars.authorizationserver.Main;

import static org.springframework.experimental.boot.server.exec.MavenClasspathEntry.springBootStarter;

@TestConfiguration(proxyBeanMethods = false)
class TestOauth2LoginMain {

	@Bean
	@OAuth2ClientProviderIssuerUri
	static CommonsExecWebServerFactoryBean authorizationServer() {
		// @formatter:off
		return CommonsExecWebServerFactoryBean.builder()
			.mainClass(Main.class.getName())
			.classpath((classpath) -> classpath
				.entries(springBootStarter("oauth2-authorization-server"))
				.scan(Main.class)
			);
		// @formatter:on
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.from(Oauth2LoginMain::main)
			.with(TestOauth2LoginMain.class)
			.run(args);
	}

}