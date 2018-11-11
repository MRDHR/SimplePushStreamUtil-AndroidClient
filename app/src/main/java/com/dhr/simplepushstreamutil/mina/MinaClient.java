package com.dhr.simplepushstreamutil.mina;


import com.dhr.simplepushstreamutil.activity.MainActivity;
import com.dhr.simplepushstreamutil.bean.FromClientBean;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

/**
 * mina客户端
 *
 * @author aniyo
 * blog:http://aniyo.iteye.com
 */
public class MinaClient {
    private String host;
    private static int port = 9123;
    private IoSession session = null;
    private IoConnector connector;
    private MainActivity mainActivity;

    public MinaClient(MainActivity mainActivity, String host) {
        this.mainActivity = mainActivity;
        this.host = host;
    }

    public void open() {
        connector = new NioSocketConnector();// 提供客户端实现

        connector.setConnectTimeout(3000);// 设置超时时间
        // 设置过滤器(编码和解码)
        ProtocolCodecFilter filter = new ProtocolCodecFilter(new ObjectSerializationCodecFactory());
        connector.getFilterChain().addLast("objectFilter", filter);
        // 业务处理
        connector.setHandler(new MinaClientHandler(mainActivity));
        // 设置session属性,获取服务端连接
        ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
        future.awaitUninterruptibly();// 等待我们的连接

        session = future.getSession();

    }

    public void send(FromClientBean fromClientBean) {
        if (null != session) {
            session.write(fromClientBean);// 写入数据,发往服务端
        }
    }

    public void close() {
        if (null != session) {
            session.closeNow();
        }
        if (null != connector) {
            connector.dispose();// 释放资源
        }
    }

}