package com.mychat.Balance;

import com.mychat.constants.ServerConstants;
import com.mychat.entity.ImNode;
import com.mychat.util.JsonUtil;
import com.mychat.zk.CuratorZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class ImLoadBalance {
    private CuratorFramework client = null;
    private String managerPath;

    public ImLoadBalance(CuratorZKclient curatorZKClient) {
        this.client = curatorZKClient.getClient();
        managerPath = ServerConstants.MANAGE_PATH;
    }

    public ImNode getBestWorker() {
        List<ImNode> workers = getWorkers();
        workers.stream().forEach(node -> log.info("node inf：{}", JsonUtil.pojoToJson(node)));
        ImNode best = balance(workers);
        return best;
    }

    protected ImNode balance(List<ImNode> items) {
        if (items.size() > 0) {
            Collections.sort(items);
            ImNode node = items.get(0);
            log.info("selected node：{}", JsonUtil.pojoToJson(node));
            return node;
        } else {
            return null;
        }
    }

    public List<ImNode> getWorkers() {
        List<ImNode> workers = new ArrayList<>();
        List<String> children = null;
        try {
            children = client.getChildren().forPath(managerPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        for (String child : children) {
            log.info("child:", child);
            byte[] payload = null;
            try {
                payload = client.getData().forPath(managerPath + "/" + child);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == payload) {
                continue;
            }
            ImNode node = JsonUtil.jsonBytes2Object(payload, ImNode.class);
            node.setId(getIdByPath(child));
            workers.add(node);
        }
        return workers;
    }
    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("znode path error");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX_NO_STRIP);
        if (index >= 0) {
            index += ServerConstants.PATH_PREFIX_NO_STRIP.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }
        if (null == sid) {
            throw new RuntimeException("node id failed");
        }
        return Long.parseLong(sid);
    }

    public void removeWorkers() {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(managerPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}