package greedy.greedybot.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfiguration.class);

    @Bean
    RestClient restClient() {
        return RestClient.builder()
                .defaultHeaders(headers -> headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .requestInterceptor((request, body, execution) -> {
                    log.info("[REQUEST]: {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .build();
    }
}
