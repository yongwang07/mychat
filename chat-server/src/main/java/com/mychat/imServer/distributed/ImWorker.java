package com.mychat.imServer.distributed;

import com.mychat.constants.ServerConstants;
import com.mychat.entity.ImNode;
import com.mychat.util.JsonUtil;
import com.mychat.zk.CuratorZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

@Data
@Slf4j
public class ImWorker {
    private CuratorFramework client = null;

    private String pathRegistered = null;

    private ImNode localNode = null;

    private static ImWorker singleInstance = null;
    private boolean inited=false;

    public synchronized static ImWorker getInst() {
        if (null == singleInstance) {
            singleInstance = new ImWorker();
            singleInstance.localNode = new ImNode();
        }
        return singleInstance;
    }

    private ImWorker() { }

    public synchronized void init() {
        if(inited) {
            return;
        }
        inited = true;
        if (null == client) {
            this.client = CuratorZKclient.instance.getClient();
        }
        if (null == localNode) {
            localNode = new ImNode();
        }
        createParentIfNeeded(ServerConstants.MANAGE_PATH);
        try {
            byte[] payload = JsonUtil.object2JsonBytes(localNode);
            pathRegistered = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(ServerConstants.PATH_PREFIX, payload);
            localNode.setId(getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLocalNode(String ip, int port) {
        localNode.setHost(ip);
        localNode.setPort(port);
    }

    public long getId() {
        return getIdByPath(pathRegistered);
    }

    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("zk node path error");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += ServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }
        if (null == sid) {
            throw new RuntimeException("zk node id failed");
        }
        return Long.parseLong(sid);
    }

    public boolean incBalance() {
        if (null == localNode) {
            throw new RuntimeException("Node node failed");
        }
        while (true) {
            try {
                localNode.incrementBalance();
                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public boolean decrBalance() {
        if (null == localNode) {
            throw new RuntimeException("znode failed");
        }
        while (true) {
            try {
                localNode.decrementBalance();
                byte[] payload = JsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void createParentIfNeeded(String managePath) {
        try {
            Stat stat = client.checkExists().forPath(managePath);
            if (null == stat) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withProtection()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(managePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ImNode getLocalNodeInfo() {
        return localNode;
    }
}