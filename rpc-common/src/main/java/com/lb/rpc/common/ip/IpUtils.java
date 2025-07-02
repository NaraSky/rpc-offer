package com.lb.rpc.common.ip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class IpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);

    public static InetAddress getLocalInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            LOGGER.error("get local ip address throws exception: {}", e);
        }
        return null;
    }

    public static String getLocalAddress() {
        return getLocalInetAddress().toString();
    }

    public static String getLocalHostName() {
        return getLocalInetAddress().getHostName();
    }

    public static String getLocalHostIp() {
        return getLocalInetAddress().getHostAddress();
    }
}
