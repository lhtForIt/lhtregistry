package io.github.lhtforit.lhtregistry;

import io.github.lhtforit.lhtregistry.cluster.Cluster;
import io.github.lhtforit.lhtregistry.cluster.Server;
import io.github.lhtforit.lhtregistry.cluster.Snapshot;
import io.github.lhtforit.lhtregistry.model.InstanceMeta;
import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import io.github.lhtforit.lhtregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import java.util.List;

/**
 * @author Leo
 * @date 2024/04/22
 */
@RestController
@Slf4j
public class LhtRegistryController {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private Cluster cluster;

    @RequestMapping("/register")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.info(" ===> register {} @ {}", service, instance);
        checkLeader();//TODO 可以做成直接转发给主而不报错
        return registryService.register(service, instance);
    }

    private void checkLeader() {
        if(!cluster.self().isLeader()) {
            throw new RuntimeException("current server is not a leader, the leader is " + cluster.leader().getUrl());
        }
    }

    @RequestMapping("/unregister")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> unregister {} @ {}", service, instance);
        checkLeader();
        return registryService.unregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestBody InstanceMeta instance, @RequestParam String service) {
        log.info(" ===> renew {} @ {}", service, instance);
        checkLeader();
        return registryService.renew(instance, service);
    }

    //多个用逗号隔开
    @RequestMapping("/renews")
    public long renews(@RequestBody InstanceMeta instance, @RequestParam String services) {
        log.info(" ===> renew {} @ {}", services, instance);
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public long version(@RequestParam String service) {
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String... services) {
        return registryService.versions(services);
    }

    @RequestMapping("/info")
    public Server self(){
        log.info(" ===> info: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot(){
        Snapshot snapshot = LhtRegistryService.snapshot();
        log.info(" ===> snapshot: {}", snapshot);
        return snapshot;
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> info: {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ===> leader: {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/sl")
    public Server sl() {
        cluster.self().setLeader(true);
        log.info(" ===> leader: {}", cluster.self());
        return cluster.self();
    }


}
