package io.swkoreatech.kosp.harvester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = {"io.swkoreatech.kosp"})
@EnableScheduling
public class HarvesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HarvesterApplication.class, args);
    }
}
