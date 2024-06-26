package io.github.lhtforit.lhtregistry;

import io.github.lhtforit.lhtregistry.cluster.Cluster;
import io.github.lhtforit.lhtregistry.healthChecker.HealthChecker;
import io.github.lhtforit.lhtregistry.healthChecker.LhtHealthChecker;
import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import io.github.lhtforit.lhtregistry.service.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Configuration
public class LhtRegistryConfig {

    @Bean
    public RegistryService registryService()
    {
        return new LhtRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(RegistryService registryService){
        return new LhtHealthChecker(registryService);
    }

    @Bean(initMethod = "init")
    public Cluster cluster(LhtRegistryConfigProperties lhtRegistryConfigProperties) {
        return new Cluster(lhtRegistryConfigProperties);
    }




}
