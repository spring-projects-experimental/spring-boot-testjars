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
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ResourceClasspathEntry}.
 *
 * @author Rob Winch
 */
class ResourceClasspathEntryTests {

	@Test
	void resolvesClass() {
		ResourceClasspathEntry entry = new ResourceClasspathEntry(ToAddToClasspath.class);
		try {
			List<String> result = entry.resolve();
			assertThat(result).hasSize(1);
			String basePath = result.get(0);
			assertThat(new File(basePath, "org/springframework/experimental/boot/server/exec/ToAddToClasspath.class"))
					.exists();
		}
		finally {
			entry.cleanup();
		}
	}

}
