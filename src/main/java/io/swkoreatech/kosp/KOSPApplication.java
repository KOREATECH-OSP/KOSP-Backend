package io.swkoreatech.kosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class KOSPApplication {

    public static void main(String[] args) {
        SpringApplication.run(KOSPApplication.class, args);
    }

}
