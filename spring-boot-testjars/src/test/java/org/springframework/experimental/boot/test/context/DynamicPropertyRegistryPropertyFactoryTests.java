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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicPropertyRegistryPropertyFactoryTests {

	private static final String NAME = "spring.security.oauth2.client.provider.spring.issuer-uri";

	private DynamicPropertyRegistryPropertyFactory propertyFactory = new DynamicPropertyRegistryPropertyFactory();

	@Test
	void dynamicPropertyWhenDefault() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("dynamicProperty");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("foo");
		assertThat(registryProperty.value().get()).isEqualTo("bar");
	}

	@Test
	void issuerUriWhenDefaults() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUri");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo(NAME);
		assertThat(registryProperty.value().get()).isEqualTo("http://127.0.0.1:1234");
	}

	@Test
	void issuerUriWhenOverrideValue() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUriWithOverriddenValue");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo(NAME);
		assertThat(registryProperty.value().get()).isEqualTo("http://localhost:1234");
	}

	@Test
	void issuerUriWhenOverrideProviderName() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUriWithOverriddenProviderName");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("spring.security.oauth2.client.provider.providerName.issuer-uri");
		assertThat(registryProperty.value().get()).isEqualTo("http://127.0.0.1:1234");
	}

	@Test
	void issuerUriWhenValueWithVariable() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("valueWithVariable");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("message");
		assertThat(registryProperty.value().get()).isEqualTo("Hello Rob");
	}

	@Test
	void dynamicPortUrlWhenDefault() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("dynamicPortUrlWithDefault");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("message.url");
		assertThat(registryProperty.value().get()).isEqualTo("http://localhost:1234");
	}

	@Test
	void dynamicPortUrlWhenOverride() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("dynamicPortUrlWithOverride");
		DynamicPropertyRegistryProperty registryProperty = this.propertyFactory.createRegistryProperty(dynamicProperty,
				() -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("message.url");
		assertThat(registryProperty.value().get()).isEqualTo("http://127.0.0.1:1234/messages");
	}

	private MergedAnnotation<DynamicProperty> dynamicPropertyFrom(String methodName) throws NoSuchMethodException {
		MergedAnnotations mergedAnnotations = MergedAnnotations.from(getClass().getDeclaredMethod(methodName));
		return mergedAnnotations.get(DynamicProperty.class);
	}

	@DynamicProperty(name = "foo", value = "'bar'")
	static void dynamicProperty() {

	}

	@OAuth2ClientProviderIssuerUri
	static void issueUri() {

	}

	@OAuth2ClientProviderIssuerUri(host = "localhost")
	static void issueUriWithOverriddenValue() {

	}

	@OAuth2ClientProviderIssuerUri(providerName = "providerName")
	static void issueUriWithOverriddenProviderName() {

	}

	@ValueWithVariable(firstName = "Rob")
	static void valueWithVariable() {

	}

	@DynamicPortUrl(name = "message.url")
	static void dynamicPortUrlWithDefault() {
	}

	@DynamicPortUrl(name = "message.url", host = "127.0.0.1", contextRoot = "/messages")
	static void dynamicPortUrlWithOverride() {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@DynamicProperty(name = "message", value = "'Hello {firstName}'")
	@interface ValueWithVariable {

		String firstName();

	}

}
