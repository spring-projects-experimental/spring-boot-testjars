/*
 * Copyright 2012-2024 the original author or authors.
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

package org.springframework.experimental.boot.server.exec.detector;

import org.springframework.lang.Nullable;

final class ClassUtils {

	/**
	 * Wrapper for Class.forName to allow mocking in tests. Note that we cannot use
	 * Spring's ClassUtils because it will not be available on the classpath.
	 * @param name the name of the Class
	 * @param classLoader the class loader to use (can be {@code null}, which indicates
	 * the default class loader)
	 * @return a class instance for the supplied name
	 * @throws ClassNotFoundException if the class was not found
	 * @throws LinkageError if the class file could not be loaded
	 * @see Class#forName(String, boolean, ClassLoader)
	 */
	@SuppressWarnings("deprecation")
	static Class<?> forName(String name, @Nullable ClassLoader classLoader)
			throws ClassNotFoundException, LinkageError {

		if (name == null) {
			throw new IllegalArgumentException("name must not be null");
		}

		return Class.forName(name, false, classLoader);
	}

	private ClassUtils() {
	}

}
