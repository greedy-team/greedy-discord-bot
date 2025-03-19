package greedy.greedybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GreedyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreedyBotApplication.class, args);
    }

}
