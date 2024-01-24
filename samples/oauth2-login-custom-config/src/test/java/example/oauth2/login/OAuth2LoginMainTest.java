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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestOauth2LoginMain.class)
public class OAuth2LoginMainTest {
	@Test
	void authorizationServerAccessOpenIdConfiguration(
			@Value("${spring.security.oauth2.client.provider.spring.issuer-uri}") String issuerUri) {
		String oidcMetadataUrl = issuerUri + "/.well-known/openid-configuration";
		RestClient restClient = RestClient.create();
		// @formatter:off
		ResponseEntity<String> result = restClient.get()
			.uri(oidcMetadataUrl)
			.retrieve()
			.toEntity(String.class);
		// @formatter:on
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void customConfiguration(@Value("${spring.security.oauth2.client.provider.spring.issuer-uri}") String issuerUri) {
		String url = issuerUri + "/hello";
		RestClient restClient = RestClient.create();
		// @formatter:off
		ResponseEntity<String> result = restClient.get()
			.uri(url)
			.headers((headers) -> headers.setBasicAuth("user", "password"))
			.retrieve()
			.toEntity(String.class);
		// @formatter:on
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo("Hello!");
	}
}
