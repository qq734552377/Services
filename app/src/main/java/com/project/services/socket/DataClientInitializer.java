package com.project.services.socket;

import com.project.services.socket.MessageCallback.CallbackHandle;
import com.project.services.socket.MessageCallback.IMsgCallback;
import com.project.services.socket.MessageProtocol.StationPackage;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Administrator on 2016/2/4.
 */
public class DataClientInitializer extends ChannelInitializer {

    public IMsgCallback callback;

    public DataClientInitializer() {
        callback = new CallbackHandle();
    }

    public void initChannel(Channel channel) {
        StationPackage stationPackage = new StationPackage(channel);
        stationPackage.callback = callback;
        TcpClientHandle handle = new TcpClientHandle(stationPackage);
        channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(30000, 0,0, TimeUnit.MILLISECONDS));
        channel.pipeline().addLast("handler", handle);
    }

}
