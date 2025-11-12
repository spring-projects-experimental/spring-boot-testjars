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

package org.springframework.experimental.boot.server.exec;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileClasspathEntry implements ClasspathEntry {

	private final File file;

	public FileClasspathEntry(String filePath) {
		File file = new File(filePath);
		if (!file.isDirectory() && !file.getName().toLowerCase().endsWith("jar")) {
			String absolutePath = file.getAbsolutePath();
			throw new IllegalArgumentException("File must be a jar file or directory '" + absolutePath + "'");
		}
		this.file = file;
	}

	public FileClasspathEntry(File file) {
		this.file = file;
	}

	public List<String> resolve() {
		String absolutePath = this.file.getAbsolutePath();
		if (!this.file.exists()) {
			throw new IllegalStateException("Could not find file to add to the classpath '" + absolutePath + "'");
		}
		return Arrays.asList(absolutePath);
	}

}
