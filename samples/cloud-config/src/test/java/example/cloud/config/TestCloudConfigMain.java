package example.cloud.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean;
import org.springframework.experimental.boot.server.exec.MavenClasspathEntry;
import org.springframework.experimental.boot.server.exec.ResourceClasspathEntry;
import org.springframework.experimental.boot.test.context.CloudConfigUri;

@TestConfiguration(proxyBeanMethods = false)
public class TestCloudConfigMain {
    @Bean
    @CloudConfigUri
    static CommonsExecWebServerFactoryBean configServer() {
        // @formatter:off
        return CommonsExecWebServerFactoryBean.builder()
                .useGenericSpringBootMain()
                .setAdditionalBeanClassNames("org.springframework.cloud.config.server.config.ConfigServerConfiguration")
                .systemProperties(props -> {
                    props.put("spring.config.location", "classpath:/application.yml");
                })
                .classpath((classpath) -> classpath
                        .entries(new MavenClasspathEntry("org.springframework.cloud:spring-cloud-config-server:4.2.1"))
                        .entries(new ResourceClasspathEntry("config-native/application.yml", "config-native/application.yml"))
                        .entries(new ResourceClasspathEntry("testjars/configServer/application.yml", "application.yml"))
                );
        // @formatter:on
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.from(CloudConfigMain::main)
                .with(TestCloudConfigMain.class)
                .run(args);
    }
}
