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

/**
 * Allows adding new properties to the environment using SpEL that has a root object of the bean that it annotations.
 * For example, assuming {@code WebServer.getPort()} exists and returns {@code 1234} the following will assign a
 * property named {@code service.url} with a value of {@code http://localhost:8080}:
 *
 * <code>
 * @Bean
 * @DynamicProperty(name = "service.url", value = "'http://localhost:' + port")
 * WebServer messageService() {
 *     ...
 * }
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicProperty {

	/**
	 * The name of the property to add
	 * @return
	 */
	String name();

	/**
	 * A SpEL expression that has a root object of the bean that it annotations.
	 * @return a SpEL expression that has a root object of the bean that it annotations. For example,
	 * "'http://localhost:' + port".
	 */
	String value();
}
