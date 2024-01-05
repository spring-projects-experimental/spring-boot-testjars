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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.experimental.boot.server.exec.ApplicationPortFileWatcher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPortFileWatcherTests {

	@Test
	void getApplicationPortWhenAlreadyWritten(@TempDir Path tempDir) throws Exception {
		int expectedPort = 9000;
		Path applicationPortPath = tempDir.resolve("application.port");
		File applicationPort = applicationPortPath.toFile();
		Files.writeString(applicationPortPath, String.valueOf(expectedPort));
		ApplicationPortFileWatcher watcher = new ApplicationPortFileWatcher(applicationPort);
		assertThat(watcher.getApplicationPort()).isEqualTo(expectedPort);
	}

	@Test
	void getApplicationPortWhenExistsNowThenWritten(@TempDir Path tempDir) throws Exception {
		int expectedPort = 9000;
		Path applicationPortPath = tempDir.resolve("application.port");
		File applicationPort = applicationPortPath.toFile();
		applicationPort.createNewFile();
		ApplicationPortFileWatcher watcher = new ApplicationPortFileWatcher(applicationPort);
		delay(() -> Files.writeString(applicationPortPath, String.valueOf(expectedPort)));
		assertThat(watcher.getApplicationPort()).isEqualTo(expectedPort);
	}

	@Test
	void getApplicationPortWhenDoesNotExistThenWritten(@TempDir Path tempDir) throws Exception {
		int expectedPort = 9000;
		Path applicationPortPath = tempDir.resolve("application.port");
		File applicationPort = applicationPortPath.toFile();
		assertThat(applicationPort).doesNotExist();
		ApplicationPortFileWatcher watcher = new ApplicationPortFileWatcher(applicationPort);
		delay(() -> {
			Files.writeString(applicationPortPath, String.valueOf(expectedPort));
		});
		assertThat(watcher.getApplicationPort()).isEqualTo(expectedPort);
	}

	void delay(Delayed r) {
		new Thread(r).start();
	}

	interface Delayed extends Runnable {

		void invoke() throws Exception;

		default void run() {
			try {
				synchronized (this) {
					Thread.currentThread().sleep(500);
				}
				invoke();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}