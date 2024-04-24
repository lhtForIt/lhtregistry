package io.github.lhtforit.lhtregistry.cluster;

import io.github.lhtforit.lhtregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/04/24
 */
@Data
@AllArgsConstructor
public class Snapshot {
    private LinkedMultiValueMap<String, InstanceMeta> registry;
    private Map<String, Long> versions;
    private Map<String, Long> timestamps;
    private long version;
}
