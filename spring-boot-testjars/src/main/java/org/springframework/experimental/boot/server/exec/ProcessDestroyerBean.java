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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.log.LogMessage;

/**
 * Allows to track every {@link Process} and then destroy them with {@link #destroyAll()}.
 *
 * @author Rob Winch
 */
final class ProcessDestroyerBean implements ProcessDestroyer {

	private static final Log logger = LogFactory.getLog(ProcessDestroyerBean.class);

	private final List<Process> processes = new ArrayList<>();

	@Override
	public boolean add(Process process) {
		return this.processes.add(process);
	}

	@Override
	public boolean remove(Process process) {
		return this.processes.remove(process);
	}

	@Override
	public int size() {
		return this.processes.size();
	}

	void destroyAll() {
		for (Process process : this.processes) {
			try {
				process.destroy();
			}
			catch (Throwable throwable) {
				if (logger.isDebugEnabled()) {
					logger.debug(LogMessage.format("Error destroying process %s", process), throwable);
				}
			}
		}
	}

}
