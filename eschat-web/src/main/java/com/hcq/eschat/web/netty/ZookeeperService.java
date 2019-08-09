package com.hcq.eschat.web.netty;

import com.alibaba.fastjson.JSONObject;
import com.hcq.eschat.core.listen.Init;
import com.hcq.eschat.core.zoo.ZookeeperSupport;
import netscape.javascript.JSObject;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

@Component
public class ZookeeperService extends ZookeeperSupport implements Init {



    @Value("${netty.service.port}")
    private int servicePort;


    protected void changeChildren(String path){
        try {
            List<String> children = getChildren(path);
            System.out.println(JSONObject.toJSONString(children));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void init() {
        ZookeeperService zookeeperConfig = new ZookeeperService();
        try {
            zookeeperConfig.connectZookeeper("127.0.0.1:2181");
            zookeeperConfig.createIfSent("/eschat");
            zookeeperConfig.createIfSent("/eschat/service");

            List<String> children = zookeeperConfig.getChildren("/eschat/service");

            zookeeperConfig.createTemp("/eschat/service/" + getIP() + ":" + servicePort);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getIP() throws UnknownHostException {
        InetAddress ia=null;
        try {
            ia=ia.getLocalHost();

            String localname=ia.getHostName();
            String localip=ia.getHostAddress();
            System.out.println("本机名称是："+ localname);
            System.out.println("本机的ip是 ："+localip);
            return localip;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }

}
