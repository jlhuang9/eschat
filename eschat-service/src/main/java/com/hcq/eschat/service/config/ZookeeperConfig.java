package com.hcq.eschat.service.config;

import com.alibaba.fastjson.JSONObject;
import com.hcq.eschat.service.listion.Init;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class ZookeeperConfig implements Watcher, Init {

    private ZooKeeper zookeeper;
    private static final int SESSION_TIME_OUT = 2000;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private List<String> list;

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            Event.EventType type = watchedEvent.getType();
            if (type == Event.EventType.None) {
                System.out.println("Watch received event");
                countDownLatch.countDown();
            } else if (type == Event.EventType.NodeChildrenChanged) {
                System.out.println(watchedEvent.getPath());
                System.out.println("子节点变更");
                try {
                    this.syncChildren(watchedEvent.getPath());
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    /**
     * 连接zookeeper
     *
     * @param host
     * @throws Exception
     */
    public void connectZookeeper(String host) throws Exception {
        zookeeper = new ZooKeeper(host, SESSION_TIME_OUT, this);
        countDownLatch.await(SESSION_TIME_OUT, TimeUnit.MILLISECONDS);
        System.out.println("zookeeper connection success");
    }


    /**
     * 获取路径下所有子节点
     *
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        List<String> children = zookeeper.getChildren(path, true);
        return children;
    }

    public void syncChildren(String path) throws KeeperException, InterruptedException {
        List<String> children = getChildren(path);
        list = children;
        System.out.println(JSONObject.toJSONString(list));

    }

    public void createIfSent(String path) throws KeeperException, InterruptedException {
        Stat exists = zookeeper.exists(path, true);
        if (exists == null) {
            zookeeper.create(path, path.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("不存在");
        } else {
            System.out.println("存在");
        }
    }

    public void createTemp(String path) throws KeeperException, InterruptedException {
        zookeeper.create(path, "192".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    public void init() {
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        try {
            zookeeperConfig.connectZookeeper("127.0.0.1:2181");
            List<String> children = zookeeperConfig.getChildren("/route");
            zookeeperConfig.createIfSent("/route");
            zookeeperConfig.createTemp("/route/thisip");
            TimeUnit.MILLISECONDS.sleep(1000L);
            zookeeperConfig.createTemp("/route/thisip1");
            System.out.println(123);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
