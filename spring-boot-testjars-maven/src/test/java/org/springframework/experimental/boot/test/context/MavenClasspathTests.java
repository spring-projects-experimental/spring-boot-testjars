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

package org.springframework.experimental.boot.test.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.experimental.boot.server.exec.MavenClasspathEntry.springBootStarter;

@ExtendWith(SpringExtension.class)
class MavenClasspathTests {

	@Test
	void authorizationServerAccessOpenIdConfiguration(
			@Value("${spring.security.oauth2.client.provider.spring.issuer-uri}") String issuerUri) {
		String oidcMetadataUrl = issuerUri + "/.well-known/openid-configuration";
		RestClient restClient = RestClient.create();
		ResponseEntity<String> result = restClient.get().uri(oidcMetadataUrl).retrieve().toEntity(String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@EnableDynamicProperty
	@TestConfiguration(proxyBeanMethods = false)
	static class Configuration {

		@Bean
		@OAuth2ClientProviderIssuerUri
		static CommonsExecWebServerFactoryBean authorizationServer() {
			// @formatter:off
			return CommonsExecWebServerFactoryBean.builder()
					.useGenericSpringBootMain()
					.classpath((classpath) -> classpath
						.entries(springBootStarter("oauth2-authorization-server"))
					);
			// @formatter:on
		}

	}

}
