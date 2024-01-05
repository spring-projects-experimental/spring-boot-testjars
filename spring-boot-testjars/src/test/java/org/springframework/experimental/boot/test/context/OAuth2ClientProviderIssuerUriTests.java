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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class OAuth2ClientProviderIssuerUriTests {

	@Test
	void dynamicPropertyJavadoc(@Autowired Environment environment) {
		assertThat(environment.getProperty("spring.security.oauth2.client.provider.spring.issuer-uri"))
				.isEqualTo("http://127.0.0.1:1234");
	}

	@EnableDynamicProperty
	@TestConfiguration(proxyBeanMethods = false)
	static class Configuration {

		@Bean
		@OAuth2ClientProviderIssuerUri
		static WebServer messageService() {
			return new WebServer();
		}

	}

}
