package io.github.lhtforit.lhtregistry.cluster;

import io.github.lhtforit.lhtregistry.LhtRegistryConfigProperties;
import io.github.lhtforit.lhtregistry.LhtregistryApplication;
import io.github.lhtforit.lhtregistry.http.HttpInvoker;
import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Slf4j
public class Cluster {

    LhtRegistryConfigProperties lhtRegistryConfigProperties;

    @Getter
    private List<Server> servers;

    String host;

    @Value("${server.port:8084}")
    String port;

    Server MYSELF;


    public Cluster(LhtRegistryConfigProperties lhtRegistryConfigProperties) {
        this.lhtRegistryConfigProperties = lhtRegistryConfigProperties;
    }

    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" ===> findFirstNonLoopbackHostInfo = " + host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }
        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.debug(" ===> MYSELF = " + MYSELF);

        initServers();
        new ServerHealth(this).healthCheck();
    }

    private void initServers() {
        List<Server> servers = new ArrayList<>();
        for (String url : lhtRegistryConfigProperties.getServerList()) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                servers.add(new Server(url, false, false, -1L));
            }
        }
        this.servers = servers;
    }

    public Server self(){
        MYSELF.setVersion(LhtRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader(){
        return this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).findFirst().orElse(null);
    }

}
