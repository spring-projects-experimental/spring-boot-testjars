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
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Finds beans annotated with {@link DynamicProperty} and adds the properties to the
 * Environment.
 *
 * @author Rob Winch
 */
class DynamicPropertyDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	private final DynamicPropertyRegistryPropertyFactory registryPropertyFactory;

	private final BeanFactory beanFactory;

	private final Environment environment;

	DynamicPropertyDefinitionRegistrar(BeanFactory beanFactory, Environment environment) {
		this.registryPropertyFactory = new DynamicPropertyRegistryPropertyFactory();
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
		DynamicPropertyRegistry properties = TestcontainersPropertySource.attach(this.environment);
		for (String dynamicPropertyBeanName : beanFactory.getBeanNamesForAnnotation(DynamicProperty.class)) {
			BeanDefinition dynamicPropertyBeanDefinition = registry.getBeanDefinition(dynamicPropertyBeanName);
			DynamicPropertyRegistryProperty property = createRegistryProperty(dynamicPropertyBeanDefinition,
					dynamicPropertyBeanName);
			if (property == null) {
				throw new IllegalStateException(
						"Missing @DynamicProperty annotation on BeanDefinition of " + dynamicPropertyBeanName);
			}
			properties.add(property.name(), property.value());
		}
	}

	private DynamicPropertyRegistryProperty createRegistryProperty(BeanDefinition dynamicPropertyBeanDefinition,
			String dynamicPropertyBeanName) {
		if (dynamicPropertyBeanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
			MethodMetadata metadata = annotatedBeanDefinition.getFactoryMethodMetadata();
			MergedAnnotation<DynamicProperty> dynamicPropertyMergedAnnotation = metadata.getAnnotations()
					.get(DynamicProperty.class);
			return this.registryPropertyFactory.createRegistryProperty(dynamicPropertyMergedAnnotation,
					() -> this.beanFactory.getBean(dynamicPropertyBeanName));
		}
		return null;
	}

}
