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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Creates a {@link DynamicPropertyRegistryProperty} from the {@link DynamicProperty}
 * annotation.
 *
 * @author Rob Winch
 */
class DynamicPropertyRegistryPropertyFactory {

	private final ExpressionParser parser = new SpelExpressionParser();

	DynamicPropertyRegistryProperty createRegistryProperty(MergedAnnotation<DynamicProperty> mergedAnnotation,
			Supplier<Object> rootObject) {
		DynamicProperty dynamicProperty = mergedAnnotation.isPresent() ? mergedAnnotation.synthesize() : null;
		if (dynamicProperty == null) {
			return null;
		}
		Properties annotationProperties = new Properties();
		annotationProperties.putAll(collectAttributes(mergedAnnotation));
		PropertyPlaceholderHelper propertyResolver = new PropertyPlaceholderHelper("{", "}");
		String value = propertyResolver.replacePlaceholders(dynamicProperty.value(), annotationProperties);
		String name = propertyResolver.replacePlaceholders(dynamicProperty.name(), annotationProperties);
		Expression expression = this.parser.parseExpression(value);
		return new DynamicPropertyRegistryProperty(name, () -> expression.getValue(rootObject.get()));
	}

	private Map<String, Object> collectAttributes(MergedAnnotation<?> mergedAnnotation) {
		Map<String, Object> attributes = new HashMap<>();
		for (MergedAnnotation<?> metaSource = mergedAnnotation; metaSource != null; metaSource = metaSource
				.getMetaSource()) {
			attributes.putAll(metaSource.asMap());
		}
		return attributes;
	}

}
