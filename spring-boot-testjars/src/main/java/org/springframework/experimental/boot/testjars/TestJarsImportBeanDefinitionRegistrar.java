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

package org.springframework.experimental.boot.testjars;

import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.testcontainers.properties.TestcontainersPropertySource;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.DynamicPropertyRegistry;


// similar to ServiceConnectionAutoConfigurationRegistrar
public class TestJarsImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private final BeanFactory beanFactory;

	private final Environment environment;

	public TestJarsImportBeanDefinitionRegistrar(BeanFactory beanFactory, Environment environment) {
		this.beanFactory = beanFactory;
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (this.beanFactory instanceof ConfigurableListableBeanFactory listableBeanFactory) {
			registerBeanDefinitions(listableBeanFactory, registry);
		}
	}

	private void registerBeanDefinitions(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {
		String[] commandLineBeanNames = beanFactory.getBeanNamesForType(WebServerCommandLine.class);
		DynamicPropertyRegistry properties = TestcontainersPropertySource.attach(this.environment);
		for (String cmdBeanName : commandLineBeanNames) {
			BeanDefinition cmdBeanDefinition = registry.getBeanDefinition(cmdBeanName);
			DynamicProperty dynamicProperty = null;
			if (cmdBeanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
				MethodMetadata metadata = annotatedBeanDefinition.getFactoryMethodMetadata();
				MergedAnnotation<DynamicProperty> mergedDynamicProperty = metadata.getAnnotations().get(DynamicProperty.class);
				dynamicProperty = mergedDynamicProperty.isPresent() ? mergedDynamicProperty.synthesize() : null;
			}
			else {
				throw new RuntimeException("BeanDefinition of " + cmdBeanName + " is not AnnotatedBeanDefinition");
			}
			if (dynamicProperty == null) {
				throw new RuntimeException("Missing @DynamicProperty annotation on BeanDefinition of " + cmdBeanName);
			}
			BeanDefinitionBuilder bean = BeanDefinitionBuilder.rootBeanDefinition(TestJarContainer.class);
			bean.addConstructorArgValue(properties);
			bean.addConstructorArgReference(cmdBeanName);
			bean.addConstructorArgValue(dynamicProperty);
			registry.registerBeanDefinition(cmdBeanName + "WebServer", bean.getBeanDefinition());
		}
	}

	static class TestJarContainer implements DisposableBean, FactoryBean<WebServer> {
		private final CommonsExecWebServer webServer;

		TestJarContainer(DynamicPropertyRegistry properties, WebServerCommandLine commandLine, DynamicProperty dynamicProperty) {
			this.webServer = new CommonsExecWebServer(commandLine);
			this.webServer.start();
			ExpressionParser parser = new SpelExpressionParser();
			Expression expression = parser.parseExpression(dynamicProperty.value(), null);
			properties.add(dynamicProperty.name(), () -> expression.getValue(this, String.class));
		}

		public CommonsExecWebServer getWebServer() {
			return this.webServer;
		}

		@Override
		public void destroy() throws Exception {
			this.webServer.stop();
		}

		@Override
		public WebServer getObject() throws Exception {
			return this.webServer;
		}

		@Override
		public Class<?> getObjectType() {
			return CommonsExecWebServer.class;
		}
	}
}
