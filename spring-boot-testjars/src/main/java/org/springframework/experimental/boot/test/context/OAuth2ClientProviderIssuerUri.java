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

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A composed annotation for {@link DynamicProperty} for specifying the property name
 * "spring.security.oauth2.client.provider.${providerName}.issuer-uri" such that providerName's value is specified by
 * {@link #providerName()}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DynamicProperty(name = "spring.security.oauth2.client.provider.${providerName}.issuer-uri", value = "")
public @interface OAuth2ClientProviderIssuerUri {

	/**
	 * Allows overriding the value of the property. The default is "'http://127.0.0.1:' + port".
	 * @return
	 */
	@AliasFor(annotation = DynamicProperty.class)
	String value() default "'http://127.0.0.1:' + port";

	/**
	 * Allows overriding the providerName portion of the property name.
	 * @return
	 */
	String providerName() default "spring";

}
