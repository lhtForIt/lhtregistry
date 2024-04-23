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

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    int timeout = 5_000;

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

        executor.scheduleAtFixedRate(() -> {
            try {
                //服务探活
                updateServers();
                electLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);

    }

    private void electLeader() {
        List<Server> masters = this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.debug(" ===>>> &&&&&& elect for no leader: " + servers);
            elect();
        } else if (masters.size() > 1) {
            log.debug(" ===>>> &&&&&& elect for more than one leader: " + servers);
            elect();
        } else {//只有一个leader，do nothing
            log.debug(" ===>>> no need election for leader: " + masters.get(0));
        }
    }

    /**
     * 节点选举有以下几种方法：
     * 我们暂时用第一种方式实现
     * 1.各种节点自己选，算法保证大家选的是同一个
     * 2.外部有一个分布式锁，谁拿到锁，谁是主
     * 3.分布式一致性算法，比如paxos,raft，，很复杂
     */
    private void elect() {

        Server candidate = null;
        //初始化候选者
        for (Server server : servers) {
            server.setLeader(false);
            if (server.isStatus()) {
                if (candidate == null) {
                    candidate = server;
                } else {
                    if (candidate.hashCode() > server.hashCode()) {//一开始准备写成server.getVersion() > candidate.getVersion()这样，但是可能出现版本相等的情况，会有问题，所以改成hashcode
                        candidate = server;
                    }
                }
            }
        }

        //确认主
        if (candidate != null) {
            candidate.setLeader(true);
            log.debug(" ===>>> elect for leader: " + candidate);
        } else {
            log.debug(" ===>>> elect failed for no leaders: " + servers);
        }

    }

    private void updateServers() {
        this.servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl()+"/info", Server.class);
                log.debug(" ===>>> health check success for " + serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);//调用成功就一定是活着的
                    server.setVersion(serverInfo.getVersion());
                    server.setLeader(serverInfo.isLeader());
                }
            } catch (Exception e) {
                log.debug(" ===>>> health check failed for " + server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self(){return MYSELF; }

    public Server leader(){
        return this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).findFirst().orElse(null);
    }

}
