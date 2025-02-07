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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * A composed annotation for {@link DynamicProperty} for specifying the property name
 * "spring.cloud.config.uri" to the value "http://{host}:{port}{contextRoot}" such that:
 *
 * <ul>
 * <li>port - the</li>
 * </ul>
 *
 *
 * <code>
 * &#64;Bean
 * &#64;CloudConfigUri
 * CommonsExecWebServerFactoryBean configServer() {
 *     return CommonsExecWebServerFactoryBean.builder()
 *       // ...
 *       .classpath((cp) -> cp
 *         .entries(springBootStarter("web"))
 *         .entries(new MavenClasspathEntry("org.springframework.cloud:spring-cloud-config-server:4.2.0"))
 *       );
 * }
 * </code>
 *
 * @author Rob Winch
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DynamicPortUrl(name = "spring.cloud.config.uri")
public @interface CloudConfigUri {

	/**
	 * Allows overriding the value of the property. The default is "localhost".
	 * @return the value of the property.
	 */
	@AliasFor(annotation = DynamicPortUrl.class)
	String host() default "localhost";

	/**
	 * Allows specifying a context root. The default is to have no context root.
	 * @return the name of the provider used in the property name.
	 */
	@AliasFor(annotation = DynamicPortUrl.class)
	String contextRoot() default "";

}
