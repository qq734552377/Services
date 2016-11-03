package com.project.services.socket;

import com.project.services.socket.Memory.NettyChannelMap;
import com.project.services.socket.MessageProtocol.Package;
import com.project.services.tool.ExceptionApplication;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by Administrator on 2016/2/3.
 */
public class TcpServerHandle extends ChannelInboundHandlerAdapter {

    public Package packageMessage;

    public TcpServerHandle(Package _packageMessage) {
        packageMessage = _packageMessage;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            ByteBuf buff = (ByteBuf) msg;
            int len = buff.readableBytes();
            byte[] buffer = new byte[len];
            buff.readBytes(buffer);
            if (packageMessage == null)
                return;
            packageMessage.Import(buffer, 0, len);
            ReferenceCountUtil.release(msg);
        } catch (Exception e) {
            ctx.channel().close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {//心跳检测还没有测试
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        Channel channel = ctx.channel();
        IdleStateEvent event = (IdleStateEvent) evt;
        if(event.state() == IdleState.ALL_IDLE)
        {
            channel.close();
            ExceptionApplication.gLogger.info("TcpServerHandle userEventTriggered : " + channel.id());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
        //System.out.println("服务器异常退出" + cause.getMessage());
        ExceptionApplication.gLogger.info("TcpServerHandle exceptionCaught : deblue service is exception "+ctx.channel().id()+"  cause:"+cause.getMessage());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //客户端连接
        ExceptionApplication.gLogger.info("TcpServerHandle channelActive : client is coming  "+ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelMap.Remove(ctx.channel().id().toString());
        if (packageMessage == null)
            return;
        packageMessage.Dispose();
        //客户端离线
        ExceptionApplication.gLogger.info("TcpServerHandle channelInactive : client break "+ ctx.channel().id());
    }
}
