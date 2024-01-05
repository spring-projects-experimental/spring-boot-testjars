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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.FileSystemUtils;

public class ResourceClasspathEntry implements ClasspathEntry {

	private final String existingResourceName;

	private final String classpathResourceName;

	private Path classpath;

	public ResourceClasspathEntry(String existingResourceName, String classpathResourceName) {
		this.existingResourceName = existingResourceName;
		this.classpathResourceName = classpathResourceName;
	}

	@Override
	public List<String> resolve() {
		if (this.classpath == null) {
			try {
				this.classpath = Files.createTempDirectory("classpath-");
				InputStream resource = getClass().getClassLoader().getResourceAsStream(this.existingResourceName);
				try {
					Path destination = this.classpath.resolve(this.classpathResourceName);
					destination.toFile().getParentFile().mkdirs();
					Files.copy(resource, destination, StandardCopyOption.REPLACE_EXISTING);
				}
				catch (IOException ex) {
					throw new RuntimeException("Failed to copy existingResourceName '" + this.existingResourceName
							+ "' to '" + this.classpathResourceName + "'", ex);
				}
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		return Arrays.asList(this.classpath.toFile().getAbsolutePath());
	}

	public void cleanup() {
		try {
			FileSystemUtils.deleteRecursively(this.classpath);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
