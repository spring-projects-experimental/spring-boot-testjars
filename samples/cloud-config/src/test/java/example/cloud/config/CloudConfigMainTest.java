package example.cloud.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestCloudConfigMain.class)
public class CloudConfigMainTest {
    @Test
    void configServerPropertyTest(@Value("${spring.cloud.config.uri}") String configUri) {
        String url = configUri + "/application/default";
        RestClient restClient = RestClient.create();
        ResponseEntity<String> result = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertTrue(result.getBody().contains("Hello!"));
    }
}
