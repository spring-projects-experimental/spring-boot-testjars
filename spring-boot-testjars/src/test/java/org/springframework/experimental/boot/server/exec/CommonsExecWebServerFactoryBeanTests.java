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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

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

	@Test
	void serverPortWhenSpecifiedThenNotOverridden() throws Exception {
		String expectedPort = "1234";
		String portSystemProperty = "server.port";
		CommonsExecWebServer server = CommonsExecWebServerFactoryBean.builder()
				.systemProperties((props) -> props.put(portSystemProperty, expectedPort)).getObject();
		assertThat(server.getCommandLine().getArguments()).contains("-D" + portSystemProperty + "=" + expectedPort);
	}

	// gh-53
	@Test
	void isEagerInitIsTrue() {
		CommonsExecWebServerFactoryBean factory = CommonsExecWebServerFactoryBean.builder();
		assertThat(factory.isEagerInit()).isTrue();
	}

	@Test
	void classpathWhenYmlDefaults() throws Exception {
		ClassPathResource expected = new ClassPathResource("testjars/hasYml/application.yml");
		String expectedContent = expected.getContentAsString(Charset.defaultCharset());
		CommonsExecWebServerFactoryBean factory = CommonsExecWebServerFactoryBean.builder();
		factory.setBeanName("hasYml");
		factory.classpath((cp) -> {
			List<ClasspathEntry> classpath = cp.getClasspath();
			assertClasspathContainsResourceWithContent(classpath, "application.yml", expectedContent);
		});
	}

	@Test
	void classpathWhenPropertiesDefaults() throws Exception {
		ClassPathResource expected = new ClassPathResource("testjars/hasProperties/application.properties");
		String expectedContent = expected.getContentAsString(Charset.defaultCharset());
		CommonsExecWebServerFactoryBean factory = CommonsExecWebServerFactoryBean.builder();
		factory.setBeanName("hasProperties");
		factory.classpath((cp) -> {
			List<ClasspathEntry> classpath = cp.getClasspath();
			assertClasspathContainsResourceWithContent(classpath, "application.properties", expectedContent);
		});
	}

	// gh-35
	@Test
	void classpathWhenYamlDefaults() throws Exception {
		ClassPathResource expected = new ClassPathResource("testjars/hasYaml/application.yaml");
		String expectedContent = expected.getContentAsString(Charset.defaultCharset());
		CommonsExecWebServerFactoryBean factory = CommonsExecWebServerFactoryBean.builder();
		factory.setBeanName("hasYaml");
		factory.classpath((cp) -> {
			List<ClasspathEntry> classpath = cp.getClasspath();
			assertClasspathContainsResourceWithContent(classpath, "application.yaml", expectedContent);
		});
	}

	private void assertClasspathContainsResourceWithContent(List<ClasspathEntry> classpath, String resourceName,
			String expectedContent) {
		ClasspathEntry lastEntry = classpath.get(classpath.size() - 1);
		String basePath = lastEntry.resolve().get(0);
		File yaml = new File(basePath, resourceName);
		assertThat(yaml).exists();
		assertThat(yaml).content().isEqualTo(expectedContent);
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
