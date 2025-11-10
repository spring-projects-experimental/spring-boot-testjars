/*
 * Copyright 2012-2025 the original author or authors.
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

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 */
class DynamicPropertyDefinitionRegistrarTests {

	@Test
	void importTwiceAndAllowBeanOverridingFalseThenNoException() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.register(ImportTwiceTestConfiguration.class);
			context.setAllowBeanDefinitionOverriding(false);
			context.refresh();
		}
	}

	// gh-104
	@Test
	void environmentPopulatedWhenNotInTestContext() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.register(WebServerConfiguration.class);
			context.refresh();

			String port = context.getEnvironment().getProperty("testjars.port");
			assertThat(port).isEqualTo("1234");
		}
	}

	@Configuration
	static class ImportTwiceTestConfiguration {

		@EnableDynamicProperty
		@Configuration
		static class Config1 {

		}

		@EnableDynamicProperty
		@Configuration
		static class Config2 {

		}

	}

	@Configuration
	@EnableDynamicProperty
	static class WebServerConfiguration {

		@Bean
		@DynamicProperty(name = "testjars.port", value = "port")
		static WebServer webServer() {
			return new WebServer();
		}

	}

}
