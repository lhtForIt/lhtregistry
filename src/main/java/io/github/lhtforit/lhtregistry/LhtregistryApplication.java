package io.github.lhtforit.lhtregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LhtRegistryConfigProperties.class)
public class LhtregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LhtregistryApplication.class, args);
    }

}
