package com.obiscr;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

/**
 * Proxy
 * @author wuzi
 */
public class OpenAIProxy {

    private String hostname;
    private Integer port;
    private Proxy.Type proxyType;

    public OpenAIProxy(String hostname, Integer port) {
        new OpenAIProxy(hostname, port, Proxy.Type.DIRECT);
    }

    public OpenAIProxy(String hostname, Integer port, Proxy.Type proxyType) {
        this.hostname = hostname;
        this.port = port;
        this.proxyType = proxyType;
    }

    public Proxy build() {
        SocketAddress socketAddress = new InetSocketAddress(hostname, port);
        return new Proxy(proxyType,socketAddress);
    }
}
