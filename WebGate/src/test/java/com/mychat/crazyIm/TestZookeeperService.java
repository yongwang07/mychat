package com.mychat.crazyIm;

import com.mychat.Balance.ImLoadBalance;
import com.mychat.WebGateSpringApplication;
import com.mychat.entity.ImNode;
import com.mychat.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebGateSpringApplication.class)
@Slf4j
public class TestZookeeperService {
    @Resource
    private ImLoadBalance imLoadBalance;

    @Test
    public void testGetBestWorker() throws Exception {
        ImNode bestWorker = imLoadBalance.getBestWorker();
        System.out.println("bestWorker = " + bestWorker);
        ThreadUtil.sleepSeconds(Integer.MAX_VALUE);
    }
}