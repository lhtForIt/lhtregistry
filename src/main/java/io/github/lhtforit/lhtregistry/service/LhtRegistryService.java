package io.github.lhtforit.lhtregistry.service;

import io.github.lhtforit.lhtregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Slf4j
public class LhtRegistryService implements RegistryService {

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();

    final static Map<String,Long> VERSIONS = new ConcurrentHashMap<>();

    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();

    public final static AtomicLong VERSION = new AtomicLong(0);


    @Override
    public InstanceMeta register(String serviceName, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.computeIfAbsent(serviceName, k -> new ArrayList<>());
        if (metas.contains(instance)) {
            log.info(" ====> instance {} already registered", instance.toUrl());
            instance.setStatus(true);
        } else {
            log.info(" ====> register instance {}", instance.toUrl());
            metas.add(instance);
            instance.setStatus(true);
            renew(instance, serviceName);
            VERSIONS.put(serviceName, VERSION.incrementAndGet());
        }
        return instance;
    }

    @Override
    public InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.computeIfAbsent(service, k -> new ArrayList<>());
        if (CollectionUtils.isEmpty(metas)) return null;
        log.info(" ====> unregister instance {}", instance.toUrl());
        metas.removeIf( m -> m.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> b));//toMap(key取值逻辑，value取值逻辑，key冲突取哪个)
    }
}
