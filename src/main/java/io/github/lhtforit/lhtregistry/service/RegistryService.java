package io.github.lhtforit.lhtregistry.service;

import io.github.lhtforit.lhtregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/04/22
 */
public interface RegistryService {

    /**
     * 基础功能
     */
    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta unregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstances(String service);

    /**
     * 高级功能
     */

    long renew(InstanceMeta instanceMeta, String... services);

    Long version(String service);

    Map<String, Long> versions(String... services);





}
