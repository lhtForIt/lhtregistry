package io.github.lhtforit.lhtregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Data
@ConfigurationProperties(prefix = "lhtregistry")
public class LhtRegistryConfigProperties {

    private List<String> serverList;


}
