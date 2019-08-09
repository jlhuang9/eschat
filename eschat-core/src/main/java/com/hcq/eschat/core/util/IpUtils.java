package com.hcq.eschat.core.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class IpUtils {
    public static SocketAddress string2SocketAddress(final String addr) {
        String[] s = addr.split(":");
        InetSocketAddress isa = new InetSocketAddress(s[0], Integer.parseInt(s[1]));
        return isa;
    }
}
