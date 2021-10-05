package com.mychat.imServer.distributed;

import com.mychat.constants.ServerConstants;
import com.mychat.entity.ImNode;
import com.mychat.im.common.bean.msg.ProtoMsg;
import com.mychat.imServer.protoBuilder.NotificationMsgBuilder;
import com.mychat.util.JsonUtil;
import com.mychat.util.ObjectUtil;
import com.mychat.util.ThreadUtil;
import com.mychat.zk.CuratorZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Data
@Slf4j
public class WorkerRouter {
    private CuratorFramework client = null;
    private String pathRegistered = null;
    private ImNode node = null;
    private static WorkerRouter singleInstance = null;
    private static final String path = ServerConstants.MANAGE_PATH;
    private ConcurrentHashMap<Long, PeerSender> workerMap = new ConcurrentHashMap<>();

    private BiConsumer<ImNode, PeerSender> runAfterAdd = (node, relaySender) -> {
        doAfterAdd(node, relaySender);
    };

    private  Consumer<ImNode> runAfterRemove = (node) -> {
        doAfterRemove(node);
    };

    public synchronized static WorkerRouter getInst() {
        if (null == singleInstance) {
            singleInstance = new WorkerRouter();
        }
        return singleInstance;
    }

    private WorkerRouter() { }

    private boolean init =false;

    public void init() {
        if(init) {
            return;
        }
        init =true;
        try {
            if (null == client) {
                this.client = CuratorZKclient.instance.getClient();
            }
            PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client,
                                       PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("CHILD_ADDED : " + data.getPath() + "  data:" + data.getData());
                            processNodeAdded(data);
                            break;
                        case CHILD_REMOVED:
                            log.info("CHILD_REMOVED : " + data.getPath() + "  data:" + data.getData());
                            processNodeRemoved(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("CHILD_UPDATED : " + data.getPath() + "  data:" + new String(data.getData()));
                            break;
                        default:
                            log.debug("[PathChildrenCache], path={}", data == null ? "null" : data.getPath());
                            break;
                    }
                }
            };

            childrenCache.getListenable().addListener(
                    childrenCacheListener, ThreadUtil.getIoIntenseTargetThreadPool());
            System.out.println("Register zk watcher successfully!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processNodeRemoved(ChildData data) {
        byte[] payload = data.getData();
        ImNode node = ObjectUtil.JsonBytes2Object(payload, ImNode.class);
        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);
        log.info("[TreeCache]节点删除, path={}, data={}", data.getPath(), JsonUtil.pojoToJson(node));
        if (runAfterRemove != null) {
            runAfterRemove.accept(node);
        }
    }

    private void doAfterRemove(ImNode node) {
        PeerSender peerSender = workerMap.get(node.getId());
        if (null != peerSender) {
            peerSender.stopConnecting();
            workerMap.remove(node.getId());
        }
    }

    private void processNodeAdded(ChildData data) {
              byte[] payload = data.getData();
        ImNode node = ObjectUtil.JsonBytes2Object(payload, ImNode.class);
        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);
        if (node.equals(getLocalNode())) {
            log.info("[TreeCache], path={}, data={}",
                    data.getPath(), JsonUtil.pojoToJson(node));
            return;
        }
        PeerSender relaySender = workerMap.get(node.getId());
        if (null != relaySender && relaySender.getRmNode().equals(node)) {
            return;
        }
        if (runAfterAdd != null) {
            runAfterAdd.accept(node, relaySender);
        }
    }

    private void doAfterAdd(ImNode n, PeerSender relaySender) {
        if (null != relaySender) {
            relaySender.stopConnecting();
        }
        relaySender = new PeerSender(n);
        relaySender.doConnect();
        workerMap.put(n.getId(), relaySender);
    }

    public PeerSender route(long nodeId) {
        PeerSender peerSender = workerMap.get(nodeId);
        if (null != peerSender) {
            return peerSender;
        }
        return null;
    }

    public void sendNotification(String json) {
        workerMap.keySet().stream().forEach(
                key -> {
                    if (!key.equals(getLocalNode().getId())) {
                        PeerSender peerSender = workerMap.get(key);
                        ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
                        peerSender.writeAndFlush(pkg);
                    }
                }
        );
    }

    public ImNode getLocalNode() {
        return ImWorker.getInst().getLocalNodeInfo();
    }

    public void remove(ImNode remoteNode) {
        workerMap.remove(remoteNode.getId());
    }
}