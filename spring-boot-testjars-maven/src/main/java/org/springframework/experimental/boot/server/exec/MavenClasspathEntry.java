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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;

import org.springframework.boot.SpringBootVersion;

/**
 * Resolves a maven dependency and its transitive dependencies.
 *
 * Note that much of the logic comes from <a href=
 * "https://github.com/apache/maven-resolver/blob/maven-resolver-1.9.18/maven-resolver-demos/maven-resolver-demo-snippets/src/main/java/org/apache/maven/resolver/examples/ResolveTransitiveDependencies.java">ResolveTransitiveDependencies</a>.
 *
 * @author Rob Winch
 */
public class MavenClasspathEntry implements ClasspathEntry {

	private final Log logger = LogFactory.getLog(getClass());

	private final List<RemoteRepository> repositories;

	/**
	 * The maven coordinates (e.g. "org.springframework:spring-core:6.1.0")
	 */
	public final String coords;

	/**
	 * Creates a new instance.
	 * @param coords the maven coordinates (e.g.
	 * "org.springframework.boot:spring-boot-starter-web:" +
	 * SpringBootVersion.getVersion()).
	 */
	public MavenClasspathEntry(String coords) {
		this(coords, newRepositories());
	}

	/**
	 * Creates a new instance.
	 *
	 * <code>
	 *     List$lt;RemoteRepository&gt; repositories = new ArrayList&lt;&gt;();
	 *     repositories.add(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
	 *     repositories.add(new RemoteRepository.Builder("spring-milestone", "default", "https://repo.spring.io/milestone/").build());
	 *     MavenClasspathEntry entry = new MavenClasspathEntry("org.springframework:spring-core:6.2.0-RC1", repositories);
	 * </code>
	 * @param coords the maven coordinates (e.g. *
	 * "org.springframework.boot:spring-boot-starter-web:" + *
	 * SpringBootVersion.getVersion()).
	 * @param repositories a {@link List} of the {@link RemoteRepository} instances to
	 * use.
	 */
	public MavenClasspathEntry(String coords, List<RemoteRepository> repositories) {
		this.coords = coords;
		this.repositories = repositories;
	}

	/**
	 * Creates a new instance with the provided Spring boot starter name.
	 * @param starterName the starter name (e.g. "web")
	 * @return a new instance.
	 */
	public static MavenClasspathEntry springBootStarter(String starterName) {
		return springBootDependency("spring-boot-starter-" + starterName);
	}

	/**
	 * Creates a new instance with the provided Spring Boot artifact name.
	 * @param artifactName the Spring Boot artifact name (e.g. "spring-boot-starter-web").
	 * @return a new instance.
	 */
	public static MavenClasspathEntry springBootDependency(String artifactName) {
		return new MavenClasspathEntry(
				"org.springframework.boot:" + artifactName + ":" + SpringBootVersion.getVersion());
	}

	@Override
	public List<String> resolve() {
		RepositorySystem system = newRepositorySystem();

		RepositorySystemSession session = newRepositorySystemSession(system);

		Artifact artifact = new DefaultArtifact(this.coords);

		DependencyFilter scopeFilter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME, JavaScopes.COMPILE);
		DependencyFilter optionalFilter = (node, parents) -> !node.getDependency().isOptional();
		DependencyFilter dependencyFilter = new AndDependencyFilter(scopeFilter, optionalFilter);

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, JavaScopes.RUNTIME));
		collectRequest.setRepositories(this.repositories);

		DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, dependencyFilter);

		List<String> result = new ArrayList<>();
		try {
			List<ArtifactResult> artifactResults = system.resolveDependencies(session, dependencyRequest)
					.getArtifactResults();

			for (ArtifactResult artifactResult : artifactResults) {
				result.add(artifactResult.getArtifact().getFile().getAbsolutePath());
			}
		}
		catch (Exception ex) {
			String message = "Error resolving artifact " + this.coords;
			this.logger.debug(message, ex);
			throw new RuntimeException(message, ex);
		}
		return result;
	}

	private static RepositorySystem newRepositorySystem() {
		/*
		 * Aether's components implement org.eclipse.aether.spi.locator.Service to ease
		 * manual wiring and using the prepopulated DefaultServiceLocator, we only need to
		 * register the repository connector and transporter factories.
		 */
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				// FIXME: Logger instead
				exception.printStackTrace();
			}
		});

		return locator.getService(RepositorySystem.class);
	}

	private static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		HashMap<Object, Object> sysProps = new HashMap<>(System.getProperties());
		String homeDirectory = System.getProperty("user.home");
		File mavenLocal = new File(homeDirectory, ".m2/repository");

		LocalRepository localRepo = new LocalRepository(mavenLocal);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		session.setTransferListener(new LoggingMavenTransferListener());
		session.setRepositoryListener(new LoggingMavenRepositoryListener());
		session.setSystemProperties(sysProps);
		session.setConfigProperties(sysProps);

		SimpleArtifactDescriptorPolicy policy = new SimpleArtifactDescriptorPolicy(ArtifactDescriptorPolicy.STRICT);
		session.setArtifactDescriptorPolicy(policy);

		// uncomment to generate dirty trees
		// session.setDependencyGraphTransformer( null );

		return session;
	}

	private static List<RemoteRepository> newRepositories() {
		return new ArrayList<>(Collections.singletonList(newCentralRepository()));
	}

	private static RemoteRepository newCentralRepository() {
		return new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build();
	}

}
