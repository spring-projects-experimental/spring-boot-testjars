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

package org.springframework.experimental.boot.server.exec;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CommonsExecWebServerFactoryBeanTests {

	@Test
	void classpathContainsSpringFactories() throws Exception {
		CommonsExecWebServer server = CommonsExecWebServerFactoryBean.builder().getObject();
		List<String> args = Arrays.asList(server.getCommandLine().getArguments());
		int index = args.indexOf("-classpath");
		assertThat(index).isGreaterThanOrEqualTo(0);
		assertThat(args).hasSizeGreaterThan(index + 1);
		String classpathArgs = args.get(index + 1);
		var loader = getClassLoaderFromArgs(classpathArgs);
		assertThat(loader.findResource("META-INF/spring.factories")).isNotNull();
		server.destroy();
	}

	@Test
	void getPortWhenServerFailsThenGetPortFails() throws Exception {
		try (CommonsExecWebServer server = CommonsExecWebServerFactoryBean.builder().getObject()) {
			server.start();
			assertThatException().isThrownBy(() -> server.getPort());
		}
	}

	@Test
	void getPortWhenServerFailsBeforeThenGetPortFails() throws Exception {
		try (CommonsExecWebServer server = CommonsExecWebServerFactoryBean.builder().getObject()) {
			server.start();
			server.waitForServer();
			assertThatException().isThrownBy(() -> server.getPort());
		}
	}

	@Test
	void mainClass() throws Exception {
		String mainClass = "example.Main";
		CommonsExecWebServer webServer = CommonsExecWebServerFactoryBean.builder().mainClass(mainClass).getObject();
		String[] args = webServer.getCommandLine().getArguments();
		assertThat(args[args.length - 1]).isEqualTo(mainClass);
	}

	@Test
	void mainClassWhenNull() {
		String mainClass = null;
		assertThatIllegalArgumentException()
				.isThrownBy(() -> CommonsExecWebServerFactoryBean.builder().mainClass(mainClass));
	}

	@Test
	void usesJarLauncherwhenNoMainClassDefined() throws Exception {
		CommonsExecWebServer webServer = CommonsExecWebServerFactoryBean.builder().getObject();
		String[] args = webServer.getCommandLine().getArguments();
		assertThat(args[args.length - 1])
				.isEqualTo("org.springframework.experimental.boot.server.exec.detector.JarLauncherDetector");
	}

	@Test
	void doesNotAddJarLauncherDetectorLauncherDetectorWhenMainClassDefined() throws Exception {
		String mainClass = "example.Main";
		CommonsExecWebServer server = CommonsExecWebServerFactoryBean.builder().mainClass(mainClass).getObject();
		List<String> args = Arrays.asList(server.getCommandLine().getArguments());
		int index = args.indexOf("-classpath");
		assertThat(index).isGreaterThanOrEqualTo(0);
		assertThat(args).hasSizeGreaterThan(index + 1);
		String classpathArgs = args.get(index + 1);
		URLClassLoader loader = getClassLoaderFromArgs(classpathArgs);
		assertThat(
				loader.findResource("org.springframework.experimental.boot.server.exec.detector.JarLauncherDetector"))
						.isNull();
		server.destroy();
	}

	private static URLClassLoader getClassLoaderFromArgs(String classpathArgs) throws MalformedURLException {
		var paths = new ArrayList<URL>();
		for (String path : classpathArgs.split(":")) {
			var url = new File(path).toURI().toURL();
			paths.add(url);
		}
		URLClassLoader loader = new URLClassLoader(paths.toArray(new URL[] {}), null);
		return loader;
	}

}
