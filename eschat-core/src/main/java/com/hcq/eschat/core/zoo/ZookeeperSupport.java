package com.hcq.eschat.core.zoo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ZookeeperSupport implements Watcher {
    protected ZooKeeper zookeeper;
    protected static final int SESSION_TIME_OUT = 2000;
    protected CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
            Event.EventType type = watchedEvent.getType();
            if (type == Event.EventType.None) {
                System.out.println("Watch received event");
                countDownLatch.countDown();
            } else if (type == Event.EventType.NodeChildrenChanged) {
                this.changeChildren(watchedEvent.getPath());
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
        return zookeeper.getChildren(path, true);
    }


    protected abstract void changeChildren(String path);


    public void createIfSent(String path) throws KeeperException, InterruptedException {
        Stat exists = zookeeper.exists(path, false);
        if (exists == null) {
            zookeeper.create(path, path.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    public void createTemp(String path) throws KeeperException, InterruptedException {
        zookeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }
}
