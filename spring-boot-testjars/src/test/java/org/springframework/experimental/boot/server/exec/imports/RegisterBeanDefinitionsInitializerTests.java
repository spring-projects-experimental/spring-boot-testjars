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

package org.springframework.experimental.boot.server.exec.imports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests {@link RegisterBeanDefinitionsInitializer}.
 *
 * @author Rob Winch
 */
@ExtendWith(MockitoExtension.class)
class RegisterBeanDefinitionsInitializerTests {

	@Mock
	GenericApplicationContext context;

	@Mock
	ConfigurableEnvironment environment;

	@Mock
	ClassLoader loader;

	RegisterBeanDefinitionsInitializer initializer = new RegisterBeanDefinitionsInitializer();

	@Test
	void initializeWhenNullEnvironmentEntry() {
		given(this.context.getEnvironment()).willReturn(this.environment);

		this.initializer.initialize(this.context);

		verify(this.context).getEnvironment();
		verifyNoMoreInteractions(this.context);
	}

	@Test
	void initializeWhenEmptyEnvironmentEntry() {
		given(this.context.getEnvironment()).willReturn(this.environment);
		given(this.environment.getProperty(RegisterBeanDefinitionsInitializer.ADDITIONAL_BEAN_CLASS_NAMES))
				.willReturn("");

		this.initializer.initialize(this.context);

		verify(this.context).getEnvironment();
		verifyNoMoreInteractions(this.context);
	}

	@Test
	void initializeWhenClassNotFoundException() throws Exception {
		String classNames = ClassA.class.getName();
		given(this.context.getEnvironment()).willReturn(this.environment);
		given(this.environment.getProperty(RegisterBeanDefinitionsInitializer.ADDITIONAL_BEAN_CLASS_NAMES))
				.willReturn(classNames);
		given(this.context.getClassLoader()).willReturn(this.loader);
		given(this.loader.loadClass(ClassA.class.getName())).willThrow(new ClassNotFoundException("Not found"));

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> this.initializer.initialize(this.context))
				.withMessageContaining("Unable to register bean of type " + ClassA.class.getName());
	}

	@Test
	void initializeWhenSingleClass() throws Exception {
		String classNames = ClassA.class.getName();
		given(this.context.getEnvironment()).willReturn(this.environment);
		given(this.environment.getProperty(RegisterBeanDefinitionsInitializer.ADDITIONAL_BEAN_CLASS_NAMES))
				.willReturn(classNames);
		given(this.context.getClassLoader()).willReturn(this.loader);
		Class beanClass = ClassA.class;
		given(this.loader.loadClass(ClassA.class.getName())).willReturn(beanClass);
		ArgumentCaptor<RootBeanDefinition> beanDefCaptor = ArgumentCaptor.forClass(RootBeanDefinition.class);

		this.initializer.initialize(this.context);

		verify(this.context).registerBeanDefinition(eq("testjars_" + ClassA.class.getName()), beanDefCaptor.capture());
		assertThat(beanDefCaptor.getValue().getBeanClass()).isEqualTo(ClassA.class);
	}

	@Test
	void initializeWhenMultiClass() throws Exception {
		String classNames = ClassA.class.getName() + "," + ClassB.class.getName();
		RegisterBeanDefinitionsInitializer initializer = new RegisterBeanDefinitionsInitializer();
		given(this.context.getEnvironment()).willReturn(this.environment);
		given(this.environment.getProperty(RegisterBeanDefinitionsInitializer.ADDITIONAL_BEAN_CLASS_NAMES))
				.willReturn(classNames);
		given(this.context.getClassLoader()).willReturn(this.loader);
		Class beanClassA = ClassA.class;
		given(this.loader.loadClass(ClassA.class.getName())).willReturn(beanClassA);
		Class beanClassB = ClassB.class;
		given(this.loader.loadClass(ClassB.class.getName())).willReturn(beanClassB);
		ArgumentCaptor<RootBeanDefinition> beanDefCaptor = ArgumentCaptor.forClass(RootBeanDefinition.class);

		initializer.initialize(this.context);

		verify(this.context).registerBeanDefinition(eq("testjars_" + ClassA.class.getName()), beanDefCaptor.capture());
		assertThat(beanDefCaptor.getValue().getBeanClass()).isEqualTo(ClassA.class);
		verify(this.context).registerBeanDefinition(eq("testjars_" + ClassB.class.getName()), beanDefCaptor.capture());
		assertThat(beanDefCaptor.getValue().getBeanClass()).isEqualTo(ClassB.class);
	}

	static class ClassA {

	}

	static class ClassB {

	}

}
