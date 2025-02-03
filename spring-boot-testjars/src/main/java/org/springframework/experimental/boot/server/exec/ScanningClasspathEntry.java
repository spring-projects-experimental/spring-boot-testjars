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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileSystemUtils;

/**
 * Adds resources recursively based upon the provided class.
 *
 * @author Rob Winch
 */
class ScanningClasspathEntry implements ClasspathEntry {

	private Log logger = LogFactory.getLog(getClass());

	private final String resourcePattern;

	private final Function<String, String> renameResource;

	private Path classpath;

	ScanningClasspathEntry(String baseDir) {
		this.resourcePattern = baseDir + "/**";
		this.renameResource = (name) -> {
			String prefix = baseDir + "/";
			int index = name.lastIndexOf(prefix);
			return (index > -1) ? name.substring(index + prefix.length()) : name;
		};
	}

	ScanningClasspathEntry(Class<?> clazz) {
		String resourcePath = toResourceName(clazz.getPackageName());
		this.resourcePattern = resourcePath + "/**";
		this.renameResource = (name) -> name.substring(name.lastIndexOf(resourcePath));
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
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			Path classpath = Files.createTempDirectory("classpath-");
			Resource[] resources = resolver.getResources(this.resourcePattern);
			for (Resource resource : resources) {
				String path = this.renameResource.apply(getPath(resource));
				if (!path.endsWith("/") && resource.isReadable()) {
					Path destination = classpath.resolve(path);
					destination.getParent().toFile().mkdirs();
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Copying " + path + " to " + destination);
					}
					Files.copy(resource.getInputStream(), destination);
				}
			}
			return classpath;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private String getPath(Resource resource) {
		if (resource instanceof ClassPathResource classPathResource) {
			return classPathResource.getPath();
		}
		if (resource instanceof FileSystemResource fileSystem) {
			return fileSystem.getPath();
		}
		throw new IllegalArgumentException("Resource is not a supported type. " + resource + " " + resource.getClass());
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
