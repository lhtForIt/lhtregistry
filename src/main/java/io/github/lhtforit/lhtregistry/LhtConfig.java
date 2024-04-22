package io.github.lhtforit.lhtregistry;

import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import io.github.lhtforit.lhtregistry.service.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Configuration
public class LhtConfig {


    @Bean
    public RegistryService registryService()
    {
        return new LhtRegistryService();
    }




}
