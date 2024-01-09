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

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;

/**
 * Use {@link #getApplicationPort()} to block until {@link #applicationPortFile} is
 * created and has content to read the contents of the file as an integer that represents
 * the application port.
 *
 * @author Rob Winch
 */
final class ApplicationPortFileWatcher {

	private final File applicationPortFile;

	private final Consumer<WatchService> registerWatchService;

	/**
	 * Create a new instance.
	 * @param applicationPortFile the file to read the application port from
	 * @param registerWatchService a {@link Consumer} that allows registering the
	 * {@link WatchService} used.
	 */
	ApplicationPortFileWatcher(File applicationPortFile, Consumer<WatchService> registerWatchService) {
		this.applicationPortFile = applicationPortFile;
		this.registerWatchService = registerWatchService;
	}

	/**
	 * Create a new instance.
	 * @param applicationPortFile the file to read the application port from
	 */
	ApplicationPortFileWatcher(File applicationPortFile) {
		this(applicationPortFile, (watchService) -> {
		});
	}

	/**
	 * Returns the port from the contents of the {@link #applicationPortFile} once content
	 * is available. If the {@link File} does not exist, or there is no content, then it
	 * blocks until both conditions are true.
	 * @return the port number from the {@link #applicationPortFile}
	 */
	int getApplicationPort() throws InterruptedException {
		Integer port;
		// FIXME: Add a timeout
		try (WatchService watch = FileSystems.getDefault().newWatchService()) {
			this.registerWatchService.accept(watch);
			this.applicationPortFile.getParentFile().toPath().register(watch, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			// check after we are watching for events, but before take an event if the
			// file exists already
			port = readPort();
			WatchKey watchKey;
			while (port == null) {
				try {
					watchKey = watch.take();
				}
				catch (ClosedWatchServiceException ex) {
					throw (InterruptedException) new InterruptedException().initCause(ex);
				}
				port = readPort();
				watchKey.reset();
				if (port != null) {
					return port;
				}
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return port;
	}

	private Integer readPort() throws IOException {
		if (!this.applicationPortFile.exists()) {
			return null;
		}
		String applicationPort = Files.readString(this.applicationPortFile.toPath());
		if (applicationPort == null || applicationPort.isBlank()) {
			return null;
		}
		return Integer.parseInt(applicationPort);
	}

}
