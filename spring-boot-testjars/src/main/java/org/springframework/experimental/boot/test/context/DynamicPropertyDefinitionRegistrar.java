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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.testcontainers.properties.TestcontainersPropertySource;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.function.Supplier;


/**
 * Finds beans annotated with {@link DynamicProperty} and adds the properties to the Environment.
 */
class DynamicPropertyDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private final ExpressionParser parser = new SpelExpressionParser();

	private final BeanFactory beanFactory;

	private final Environment environment;

	public DynamicPropertyDefinitionRegistrar(BeanFactory beanFactory, Environment environment) {
		this.beanFactory = beanFactory;
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (this.beanFactory instanceof ConfigurableListableBeanFactory listableBeanFactory) {
			registerBeanDefinitions(listableBeanFactory, registry);
		}
	}

	DynamicPropertyRegistryProperty createRegistryProperty(AnnotatedTypeMetadata metadata, Supplier<Object> rootObject) {
		MergedAnnotation<DynamicProperty> mergedDynamicProperty = metadata.getAnnotations().get(DynamicProperty.class);
		DynamicProperty dynamicProperty = mergedDynamicProperty.isPresent() ? mergedDynamicProperty.synthesize() : null;
		if (dynamicProperty == null) {
			return null;
		}
		Expression expression = parser.parseExpression(dynamicProperty.value());
		return new DynamicPropertyRegistryProperty(dynamicProperty.name(), () -> expression.getValue(rootObject.get()));
	}

	private void registerBeanDefinitions(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {
		DynamicPropertyRegistry properties = TestcontainersPropertySource.attach(this.environment);
		for (String dynamicPropertyBeanName : beanFactory.getBeanNamesForAnnotation(DynamicProperty.class)) {
			BeanDefinition dynamicPropertyBeanDefinition = registry.getBeanDefinition(dynamicPropertyBeanName);
			DynamicPropertyRegistryProperty property = createRegistryProperty(dynamicPropertyBeanDefinition, dynamicPropertyBeanName);
			if (property == null) {
				throw new IllegalStateException("Missing @DynamicProperty annotation on BeanDefinition of " + dynamicPropertyBeanName);
			}
			properties.add(property.name(), property.value());
		}
	}

	private DynamicPropertyRegistryProperty createRegistryProperty(BeanDefinition dynamicPropertyBeanDefinition, String dynamicPropertyBeanName) {
		if (dynamicPropertyBeanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
			MethodMetadata metadata = annotatedBeanDefinition.getFactoryMethodMetadata();
			return createRegistryProperty(metadata, () -> this.beanFactory.getBean(dynamicPropertyBeanName));
		}
		return null;
	}
	record DynamicPropertyRegistryProperty(String name, Supplier<Object> value) {
	}
}
