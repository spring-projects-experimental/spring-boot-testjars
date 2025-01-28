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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simplifies setting a property to a URL that contains a dynamic port obtained from a
 * property (default is named port) on the Bean. For example, assuming that the "messages"
 * Bean has a property named port that returns "1234", the following will add a property
 * named "messages.url" with a value of "http://localhost:1234".
 *
 * <code>
 * &#64;Bean
 * &#64;DynamicPortUrl(name = "messages.url")
 * CommonsExecWebServerFactoryBean messages() {
 *     return CommonsExecWebServerFactoryBean.builder()
 *       .classpath((cp) -> cp
 *         .files("build/libs/messages-0.0.1-SNAPSHOT.jar")
 *       );
 * }
 * </code>
 *
 * The following allows overrides the host property and context root to produce a property
 * named "messages.url" with a value of "http://127.0.0.1:1234/messages" (assuming the
 * port property returns "1234").
 *
 * <code>
 * &#64;Bean
 * &#64;DynamicPortUrl(name = "messages.url", host = "127.0.0.1", contextRoot = "/messages")
 * CommonsExecWebServerFactoryBean messages() {
 *     return CommonsExecWebServerFactoryBean.builder()
 *       .classpath((cp) -> cp
 *         .files("build/libs/messages-0.0.1-SNAPSHOT.jar")
 *       );
 * }
 * </code>
 *
 * @author Rob Winch
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DynamicProperty(name = "${name}", value = "'http://${host}:' + port + '${contextRoot}'")
public @interface DynamicPortUrl {

	/**
	 * The property name to use.
	 * @return the property name to use
	 */
	String name();

	/**
	 * The host to use (default "localhost").
	 * @return the host to use
	 */
	String host() default "localhost";

	/**
	 * The valid SpEL expression that determines the port to use for the URL (default
	 * port).
	 * @return the valid SpEL expression that determines the port to use for the URL
	 */
	String port() default "port";

	/**
	 * Specifies the context root for the URL (e.g. "/messages"). The default is an empty
	 * String.
	 * @return the context root for the URL
	 */
	String contextRoot() default "";

}
