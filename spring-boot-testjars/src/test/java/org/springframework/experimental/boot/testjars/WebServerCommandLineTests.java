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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebServerCommandLineTests {

	@Test
	void classpathContainsSpringFactories() throws Exception {
		CommonsExecWebServer server = CommonsExecWebServer.builder()
				.build();
		List<String> args = Arrays.asList(server.getCommandLine().getArguments());
		int index = args.indexOf("-classpath");
		assertThat(index).isGreaterThanOrEqualTo(0);
		assertThat(args).hasSizeGreaterThan(index + 1);
		String classpath = args.get(index + 1);
		URLClassLoader loader = new URLClassLoader(new URL[] { new File(classpath).toURI().toURL() }, null);
		assertThat(loader.findResource("META-INF/spring.factories")).isNotNull();
		server.destroy();
	}
}