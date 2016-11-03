package com.project.services.socket.MessageCallback;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/4.
 */
public interface IMsgCallback {

   void Receive(Channel channel,Object callback);



}
