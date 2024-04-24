package io.github.lhtforit.lhtregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Leo
 * @date 2024/04/24
 */
@Slf4j
public class Election {

    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.debug(" ===>>> &&&&&& elect for no leader: " + servers);
            elect(servers);
        } else if (masters.size() > 1) {
            log.debug(" ===>>> &&&&&& elect for more than one leader: " + servers);
            elect(servers);
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
    private void elect(List<Server> servers) {

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


}
