package com.example.springboottestcontainers;

import jakarta.annotation.PostConstruct;
import org.apache.commons.exec.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

public class SpringBootRunner {
	private String javaProcess;

	private String classpath;

	private ProcessDestroyer processDestroyer;

	private String mainClass = "org.springframework.boot.loader.JarLauncher";

	private File applicationPortFile = getApplicationPortFile();

	@NotNull
	private static File getApplicationPortFile() {
		try {
			return File.createTempFile("application", ".port");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void start() {
		System.out.println("Starting the application");
		ProcessHandle processHandle = ProcessHandle.current();
		String currentCommand = processHandle.info().command().get();
		String resources = ":/home/rwinch/code/rwinch/spring-boot-test-containers/src/main/resources";
		String line = currentCommand + " -Dserver.port=0 -DPORTFILE='" + applicationPortFile.getAbsolutePath() + "' -Dfile.encoding=UTF-8 -classpath /home/rwinch/code/rwinch/spring-authorization-server-sample/build/classes/java/main:/home/rwinch/code/rwinch/spring-authorization-server-sample/build/resources/main:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-oauth2-authorization-server/3.1.2/db2b01032ab37cafdf4495d54eca84533291ace7/spring-boot-starter-oauth2-authorization-server-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-web/3.1.2/30b824817e764a5a5a1e9fb46e7ace40bcfa3185/spring-boot-starter-web-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-oauth2-authorization-server/1.1.1/be104045fe82f2da6b65dd9a426bc2e6f7a9e26e/spring-security-oauth2-authorization-server-1.1.1.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-json/3.1.2/c48d521879dbbcebe1a99f47257612968e022f5b/spring-boot-starter-json-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter/3.1.2/c09a48df6fbc2b07b7aef1256b45260a3478b49f/spring-boot-starter-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-tomcat/3.1.2/2e7284f8ecf2989e1a6ffc7faf964eccb309bfd4/spring-boot-starter-tomcat-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-webmvc/6.0.11/892b4a63b5d930c2c43058c6db0c2c65401fb078/spring-webmvc-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-web/6.0.11/c32a7bf6b88b83bac6aa63866fcf208892640446/spring-web-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-oauth2-resource-server/6.1.2/d5ec31f7aaaa4c130e7e288631da7809df8cad67/spring-security-oauth2-resource-server-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-web/6.1.2/c1f133de05e895e58b34461f6b1189a8d6a0d56a/spring-security-web-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-oauth2-jose/6.1.2/693cecf1b5ba37676e26fafd1fb7dec6c6feccf/spring-security-oauth2-jose-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-oauth2-core/6.1.2/549959728898b2d5e7880a502c536e3cc6e68b7c/spring-security-oauth2-core-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-config/6.1.2/f6391123f7c3cb7a158266fc81cc4c82c5c4fe19/spring-security-config-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-core/6.0.11/87834b5cf95c6fa28f5bdf8a85e0daf0bff918a8/spring-core-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.nimbusds/nimbus-jose-jwt/9.31/229ba7b31d1f886968896c48aeeba5a1586b00bc/nimbus-jose-jwt-9.31.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-databind/2.15.2/9353b021f10c307c00328f52090de2bdb4b6ff9c/jackson-databind-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.datatype/jackson-datatype-jsr310/2.15.2/30d16ec2aef6d8094c5e2dce1d95034ca8b6cb42/jackson-datatype-jsr310-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.module/jackson-module-parameter-names/2.15.2/75f8d2788db20f6c587c7a19e94fb6248c314241/jackson-module-parameter-names-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/2.15.2/66a50e089cfd2f93896b9b6f7a734cea7bcf2f31/jackson-datatype-jdk8-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-autoconfigure/3.1.2/ea294976f925441fc1d5a5414d5d31717f06aa3c/spring-boot-autoconfigure-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot/3.1.2/3cf070561716277ec91ebadc07362dd0b4a9f63f/spring-boot-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-logging/3.1.2/4e47bd132fdd0d60ad57d70bdc99198b182166ee/spring-boot-starter-logging-3.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/jakarta.annotation/jakarta.annotation-api/2.1.1/48b9bda22b091b1f48b13af03fe36db3be6e1ae3/jakarta.annotation-api-2.1.1.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.yaml/snakeyaml/1.33/2cd0a87ff7df953f810c344bdf2fe3340b954c69/snakeyaml-1.33.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.apache.tomcat.embed/tomcat-embed-websocket/10.1.11/ac9ecc8a2b6a2cd8123554259faabc2e7b4aa013/tomcat-embed-websocket-10.1.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.apache.tomcat.embed/tomcat-embed-core/10.1.11/9d1ba230cbe0dfb410b0beba102eff20e14793a1/tomcat-embed-core-10.1.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.apache.tomcat.embed/tomcat-embed-el/10.1.11/97aa65b1f036f722e869f582f8d607d01508d420/tomcat-embed-el-10.1.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-context/6.0.11/a7b10f3d3c1492bfc4e6d7c966cd2e21f4d441f5/spring-context-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-aop/6.0.11/15b85c825558fadb85e3c77779d7225ce7e4a7bc/spring-aop-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-beans/6.0.11/8513efc6e94b407b5cd85f69eeec511f1ef34164/spring-beans-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-expression/6.0.11/1d0940120a275719c3988c592068f4acf807fe59/spring-expression-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/io.micrometer/micrometer-observation/1.11.2/704e145c4801320b4c7abf6c1a5b000155b069db/micrometer-observation-1.11.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-core/6.1.2/acd42f051b0f38d043ad807ca542f1f53bfa2fe/spring-security-core-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework/spring-jcl/6.0.11/c9b16cdb6d4874ba4118fcdd4b0335f6278b378/spring-jcl-6.0.11.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.github.stephenc.jcip/jcip-annotations/1.0-1/ef31541dd28ae2cefdd17c7ebf352d93e9058c63/jcip-annotations-1.0-1.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-annotations/2.15.2/4724a65ac8e8d156a24898d50fd5dbd3642870b8/jackson-annotations-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-core/2.15.2/a6fe1836469a69b3ff66037c324d75fc66ef137c/jackson-core-2.15.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-classic/1.4.8/f00ba91d993e4d14301b11968d3cacc3be7ef3e1/logback-classic-1.4.8.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-to-slf4j/2.20.0/d37f81f8978e2672bc32c82712ab4b3f66624adc/log4j-to-slf4j-2.20.0.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.slf4j/jul-to-slf4j/2.0.7/a48f44aeaa8a5ddc347007298a28173ac1fbbd8b/jul-to-slf4j-2.0.7.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/io.micrometer/micrometer-commons/1.11.2/f03f87bd7eacf79b00aa89907c6e30e0ec86b4/micrometer-commons-1.11.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.springframework.security/spring-security-crypto/6.1.2/3acab1c7979cc9d44d5e12e7f5acd4d9552e6b83/spring-security-crypto-6.1.2.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-core/1.4.8/3fba9c105e0efc5ffdcda701379687917d5286f7/logback-core-1.4.8.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/2.0.7/41eb7184ea9d556f23e18b5cb99cad1f8581fc00/slf4j-api-2.0.7.jar:/home/rwinch/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.20.0/1fe6082e660daf07c689a89c94dc0f49c26b44bb/log4j-api-2.20.0.jar:/home/rwinch/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/231.9011.34/lib/idea_rt.jar" + resources + " com.example.springauthorizationserversample.SpringAuthorizationServerSampleApplication";
		System.out.println(line);
		CommandLine cmdLine = CommandLine.parse(line);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		System.out.println("Execute");
		DefaultExecuteResultHandler result = new DefaultExecuteResultHandler();
		try {
			executor.execute(cmdLine, null, result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to run the command", e);
		}
	}

	public int getPort() {
		System.out.println("Start");
		try (WatchService ws = FileSystems.getDefault().newWatchService()) {
			this.applicationPortFile.getParentFile().toPath().register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
			System.out.println("Waiting for server port " + this.applicationPortFile);
			while (true) {
				try {
					ws.take();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				if (!this.applicationPortFile.exists()) {
					continue;
				}
				String applicationPort = Files.readString(this.applicationPortFile.toPath());
				if (applicationPort == null || applicationPort.isBlank()) {
					continue;
				}
				int port = Integer.parseInt(applicationPort);
				return port;
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUrl() {
		String result = "http://localhost:" + getPort() ;
		return result;
	}
}
