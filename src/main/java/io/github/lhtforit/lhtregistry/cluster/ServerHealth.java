package io.github.lhtforit.lhtregistry.cluster;

import io.github.lhtforit.lhtregistry.http.HttpInvoker;
import io.github.lhtforit.lhtregistry.service.LhtRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/24
 */
@Slf4j
public class ServerHealth {

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    int timeout = 5_000;

    Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void healthCheck() {
        executor.scheduleAtFixedRate(() -> {
            try {
                //服务探活,更新server状态
                updateServers();
                //选主
                doElect();
                //同步快照
                syncFromLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void doElect() {
        new Election().electLeader(this.cluster.getServers());
    }

    private void syncFromLeader() {
        Server leader = cluster.leader();
        Server self = cluster.self();
        log.debug(" ===>>> leader version: " + leader.getVersion() + ", my version: " + self.getVersion());
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ===>>> sync snapshot from leader: " + leader);
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            log.debug(" ===>>> sync and restore snapshot: " + snapshot);
            LhtRegistryService.restore(snapshot);
        }
    }



    private void updateServers() {
        this.cluster.getServers().forEach(server -> {
            try {
                if (server.equals(this.cluster.self())) return;
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


}
