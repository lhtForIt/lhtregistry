package io.github.lhtforit.lhtregistry.healthChecker;

import io.github.lhtforit.lhtregistry.model.InstanceMeta;
import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import io.github.lhtforit.lhtregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Slf4j
public class LhtHealthChecker implements HealthChecker{

    RegistryService registryService;

    public LhtHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    long timeout = 20_000;

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            log.info(" ===> Health checker running...");
            long now = System.currentTimeMillis();
            LhtRegistryService.TIMESTAMPS.keySet().stream().forEach(serviceAndInstance -> {
                Long pre = LhtRegistryService.TIMESTAMPS.get(serviceAndInstance);
                if (now - pre > timeout) {
                    log.info(" ===> Health checker: {} is down", serviceAndInstance);
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String instance = serviceAndInstance.substring(index + 1);
                    registryService.unregister(service, InstanceMeta.from(instance));
                    LhtRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
