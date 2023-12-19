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

/**
 * Represents a Spring Boot server application.
 *
 * FIXME: Should this be {@link org.springframework.boot.web.server.WebServer}?
 */
public interface SpringBootServer {

	/**
	 * Asynchronously start the application.
	 */
	void startAsync();

	/**
	 * Gets the port the application was started on. Blocks until the application will respond on the port.
	 * @return the port the application was started on.
	 */
	int getApplicationPort();

}
