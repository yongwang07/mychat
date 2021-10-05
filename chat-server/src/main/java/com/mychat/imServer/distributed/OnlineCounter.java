package com.mychat.imServer.distributed;

import com.mychat.constants.ServerConstants;
import com.mychat.zk.CuratorZKclient;
import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;

@Data
public class OnlineCounter {
    private static final String PATH = ServerConstants.COUNTER_PATH;

    private CuratorFramework client = null;

    private static OnlineCounter singleInstance = null;

    DistributedAtomicLong distributedAtomicLong = null;
    private Long curValue;

    public static OnlineCounter getInst() {
        if (null == singleInstance) {
            singleInstance = new OnlineCounter();
            singleInstance.client = CuratorZKclient.instance.getClient();
            singleInstance.init();
        }
        return singleInstance;
    }

    private void init() {
        distributedAtomicLong = new DistributedAtomicLong(client, PATH, new RetryNTimes(10, 30));
    }

    private OnlineCounter() { }

    public boolean increment() {
        boolean result = false;
        AtomicValue<Long> val = null;
        try {
            val = distributedAtomicLong.increment();
            result = val.succeeded();
            System.out.println("old cnt: " + val.preValue()
                    + "   new cnt : " + val.postValue()
                    + "  result:" + val.succeeded());
            curValue = val.postValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public boolean decrement() {
        boolean result = false;
        AtomicValue<Long> val = null;
        try {
            val = distributedAtomicLong.decrement();
            result = val.succeeded();
            System.out.println("old cnt: " + val.preValue()
                    + "   new cnt : " + val.postValue()
                    + "  result:" + val.succeeded());
            curValue = val.postValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}