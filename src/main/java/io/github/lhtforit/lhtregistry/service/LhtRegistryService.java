package io.github.lhtforit.lhtregistry.service;

import io.github.lhtforit.lhtregistry.cluster.Snapshot;
import io.github.lhtforit.lhtregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
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
    public synchronized InstanceMeta register(String serviceName, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.computeIfAbsent(serviceName, k -> new ArrayList<>());
//        convertHost(instance);
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

    private void convertHost(InstanceMeta instance) {
        if (instance.getHost().equals("127.0.0.1") || instance.getHost().equals("localhost")) {
            String host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            instance.setHost(host);
        }
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.computeIfAbsent(service, k -> new ArrayList<>());
        convertHost(instance);
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
    public synchronized long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
//        convertHost(instance);
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

    public synchronized static long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getRegistry());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVersions());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTimestamps());
        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }

    /**
     * 快照需要深拷贝，不能污染原始数据
     * @return
     */
    public synchronized static Snapshot snapshot() {
        LinkedMultiValueMap registry = new LinkedMultiValueMap();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry,versions,timestamps,VERSION.get());
    }

}
