package org.springframework.experimental.boot.test.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.experimental.boot.test.context.DynamicPropertyDefinitionRegistrar.DynamicPropertyRegistryProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DynamicPropertyDefinitionRegistrarTests {

	private static final String NAME = "spring.security.oauth2.client.provider.spring.issuer-uri";

	@InjectMocks
	private DynamicPropertyDefinitionRegistrar registrar;

	@Test
	void dynamicPropertyWhenDefault() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("dynamicProperty");
		DynamicPropertyRegistryProperty registryProperty = this.registrar.createRegistryProperty(dynamicProperty, () -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("foo");
		assertThat(registryProperty.value().get()).isEqualTo("bar");
	}

	@Test
	void issuerUriWhenDefaults() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUri");
		DynamicPropertyRegistryProperty registryProperty = this.registrar.createRegistryProperty(dynamicProperty, () -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo(NAME);
		assertThat(registryProperty.value().get()).isEqualTo("http://127.0.0.1:1234");
	}

	@Test
	void issuerUriWhenOverrideValue() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUriWithOverriddenValue");
		DynamicPropertyRegistryProperty registryProperty = this.registrar.createRegistryProperty(dynamicProperty, () -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo(NAME);
		assertThat(registryProperty.value().get()).isEqualTo("http://localhost:1234");
	}

	@Test
	void issuerUriWhenOverrideProviderName() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("issueUriWithOverriddenProviderName");
		DynamicPropertyRegistryProperty registryProperty = this.registrar.createRegistryProperty(dynamicProperty, () -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("spring.security.oauth2.client.provider.providerName.issuer-uri");
		assertThat(registryProperty.value().get()).isEqualTo("http://127.0.0.1:1234");
	}

	@Test
	void issuerUriWhenValueWithVariable() throws Exception {
		MergedAnnotation<DynamicProperty> dynamicProperty = dynamicPropertyFrom("valueWithVariable");
		DynamicPropertyRegistryProperty registryProperty = this.registrar.createRegistryProperty(dynamicProperty, () -> new WebServer());
		assertThat(registryProperty.name()).isEqualTo("message");
		assertThat(registryProperty.value().get()).isEqualTo("Hello Rob");
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

	@OAuth2ClientProviderIssuerUri("'http://localhost:' + port")
	static void issueUriWithOverriddenValue() {

	}


	@OAuth2ClientProviderIssuerUri(providerName = "providerName")
	static void issueUriWithOverriddenProviderName() {

	}

	@ValueWithVariable(firstName = "Rob")
	static void valueWithVariable() {

	}

	@Retention(RetentionPolicy.RUNTIME)
	@DynamicProperty(name = "message", value = "'Hello ${firstName}'")
	@interface ValueWithVariable {
		String firstName();
	}
}