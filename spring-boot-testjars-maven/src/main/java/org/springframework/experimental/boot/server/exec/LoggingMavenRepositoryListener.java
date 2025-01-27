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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

import org.springframework.util.Assert;

/**
 * Sends all the events to a logger at debug level.
 *
 * @author Rob Winch
 */
class LoggingMavenRepositoryListener extends AbstractRepositoryListener {

	private final Log logger = LogFactory.getLog(getClass());

	public void artifactDeployed(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Deployed " + event.getArtifact() + " to " + event.getRepository());
		}
	}

	public void artifactDeploying(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Deploying " + event.getArtifact() + " to " + event.getRepository());
		}
	}

	public void artifactDescriptorInvalid(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Invalid artifact descriptor for " + event.getArtifact() + ": "
					+ event.getException().getMessage());
		}

	}

	public void artifactDescriptorMissing(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Missing artifact descriptor for " + event.getArtifact());
		}
	}

	public void artifactInstalled(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Installed " + event.getArtifact() + " to " + event.getFile());
		}
	}

	public void artifactInstalling(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Installing " + event.getArtifact() + " to " + event.getFile());
		}
	}

	public void artifactResolved(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolved artifact " + event.getArtifact() + " from " + event.getRepository());
		}
	}

	public void artifactDownloading(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Downloading artifact " + event.getArtifact() + " from " + event.getRepository());
		}
	}

	public void artifactDownloaded(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Downloaded artifact " + event.getArtifact() + " from " + event.getRepository());
		}
	}

	public void artifactResolving(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolving artifact " + event.getArtifact());
		}
	}

	public void metadataDeployed(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Deployed " + event.getMetadata() + " to " + event.getRepository());
		}
	}

	public void metadataDeploying(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Deploying " + event.getMetadata() + " to " + event.getRepository());
		}
	}

	public void metadataInstalled(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Installed " + event.getMetadata() + " to " + event.getFile());
		}
	}

	public void metadataInstalling(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Installing " + event.getMetadata() + " to " + event.getFile());
		}
	}

	public void metadataInvalid(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Invalid metadata " + event.getMetadata());
		}
	}

	public void metadataResolved(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolved metadata " + event.getMetadata() + " from " + event.getRepository());
		}
	}

	public void metadataResolving(RepositoryEvent event) {
		assertEventNotNull(event);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Resolving metadata " + event.getMetadata() + " from " + event.getRepository());
		}
	}

	private static void assertEventNotNull(RepositoryEvent event) {
		Assert.notNull(event, "event cannot be null");
	}

}
