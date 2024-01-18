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

package org.springframework.experimental.boot.server.exec;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.util.FileSystemUtils;

/**
 * Adds resources recursively based upon the provided class.
 *
 * @author Rob Winch
 */
class RecursiveResourceClasspathEntry implements ClasspathEntry {

	private final Class<?> clazz;

	private Path classpath;

	RecursiveResourceClasspathEntry(Class<?> clazz) {
		this.clazz = clazz;
	}

	Path getClasspath() {
		return this.classpath;
	}

	@Override
	public List<String> resolve() {
		if (this.classpath == null) {
			this.classpath = createClasspath();
		}
		return Arrays.asList(this.classpath.toFile().getAbsolutePath());
	}

	private Path createClasspath() {
		ClassLoader classLoader = this.clazz.getClassLoader();
		String resourcePath = toResourceName(this.clazz.getPackageName());
		String resourceName = toResourceName(this.clazz.getName()) + ".class";
		URL resource = classLoader.getResource(resourceName);
		try {
			this.classpath = Files.createTempDirectory("classpath-");
			Path resourceParent = Paths.get(resource.toURI()).getParent();
			Path destination = this.classpath.resolve(resourcePath);
			destination.getParent().toFile().mkdirs();
			Files.copy(resourceParent, destination);
			return this.classpath;
		}
		catch (URISyntaxException | IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void cleanup() {
		try {
			FileSystemUtils.deleteRecursively(this.classpath);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static String toResourceName(String name) {
		String matcher = Pattern.quote(".");
		return name.replaceAll(matcher, "/");
	}

}
