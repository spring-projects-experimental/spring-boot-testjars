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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DynamicPropertyRegistrarBeanInitializer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class EnableDynamicPropertyTests {

	@Test
	void dynamicPropertyJavadoc(@Autowired Environment environment) {
		assertThat(environment.getProperty("service.url")).isEqualTo("http://localhost:1234");
	}

	/**
	 * Ensures the TestContext framework and {@link EnableDynamicProperty} on register a
	 * single {@link DynamicPropertyRegistrarBeanInitializer}.
	 * @param initialiers the initializers
	 */
	@Test
	void singleDynamicPropertyRegistrarBeanInitializer(
			@Autowired ObjectProvider<DynamicPropertyRegistrarBeanInitializer> initialiers) {
		assertThat(initialiers.orderedStream()).hasSize(1);
	}

	@EnableDynamicProperty
	@TestConfiguration(proxyBeanMethods = false)
	static class Configuration {

		@Bean
		@DynamicProperty(name = "service.url", value = "'http://localhost:' + port")
		static WebServer messageService() {
			return new WebServer();
		}

	}

}
