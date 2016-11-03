package com.project.services.socket;

import android.content.Intent;
import android.util.Base64;

import com.project.services.serial.MermoySerial;
import com.project.services.serial.PadSerialPort;
import com.project.services.socket.Memory.NettyChannelMap;
import com.project.services.socket.Memory.NettyClientMap;
import com.project.services.socket.TimerConnect.WhileCheckClient;
import com.project.services.tool.ExceptionApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/16.
 */
public class Common {

    public static String ConfigKey = "ServiceClient";

    public static String DeviceNumber = "185632126";

    public static String SerialName = "ttyS2";
    public static Object CheckFirstStatus = new Object();
    private static boolean  FirstStatus=false;

    public static Map<String, String> ssid_Password_Map = new HashMap<>();

    public static String encode(byte[] bstr) {
        return Base64.encodeToString(bstr, Base64.DEFAULT);
    }


    /**
     * 解码
     *
     * @param str
     * @return string
     */
    public static byte[] decode(String str) {
        try {
            return Base64.decode(str, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static void ChangeFirstStatus() {
        synchronized (Common.CheckFirstStatus) {
            Common.FirstStatus = true;
        }
    }

    public static void CheckFirst() {
        synchronized (Common.CheckFirstStatus) {
            if (Common.FirstStatus)
                return;
            //发送广播
            Common.SendFirst();
        }
    }

    public static boolean SendData(byte[] Data) {
        NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
        if (client == null)
            return false;
        return client.Send(Data);
    }

    public static boolean SendDataHead(byte[] Data) {
        NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
        if (client == null)
            return false;
        return client.Send(Data);
    }

    public static void ServicesAllSend(byte[] Data) {
        Set set = NettyChannelMap.ToList();
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Channel value = (Channel) entry.getValue();
            if (value == null)
                return;
            ByteBuf resp = Unpooled.copiedBuffer(Data);
            value.writeAndFlush(resp);
//            Log.e("calm", "---2201---------" + value.id());
        }
    }

    public static void SendChannelData(Channel channel) {
        byte[] data = Common.GetFormat("2307", 1, 1, new String[]{"1"});
        ByteBuf resp = Unpooled.copiedBuffer(data);
        channel.writeAndFlush(resp);
    }

    public static void ChannelSendBuffer(Channel channel, byte[] buffer) {
        if (channel == null)
            return;
        ByteBuf resp = Unpooled.copiedBuffer(buffer);
        channel.writeAndFlush(resp);
    }

    public static void SendSerial(byte[] Data) {
        PadSerialPort channel = MermoySerial.GetChannel(Common.SerialName);
        if (channel == null)
            return;
        channel.SendMessage(Data);
    }

    public static void SendHandle() {
        Intent sendIntent = new Intent();
        sendIntent.setAction("com.project.services.openService");
        ExceptionApplication.context.sendBroadcast(sendIntent);
        //WhileCheckClient.UpTotalNumber();
    }

    public static void SendRecovery() {
        Intent sendIntent = new Intent();
        sendIntent.setAction("com.project.services.sendRecovery");
        ExceptionApplication.context.sendBroadcast(sendIntent);
        WhileCheckClient.UpTotalNumber();
    }

    public static void SendUpdate() {
        Intent sendIntent = new Intent();
        sendIntent.setAction("com.project.services.sendNoUpdate");
        ExceptionApplication.context.sendBroadcast(sendIntent);
    }

    public static void SendStop(String ssid) {
        Intent intent = new Intent();
        intent.putExtra("SSID", ssid);
        intent.setAction("com.project.services.disConnect");
        ExceptionApplication.context.sendBroadcast(intent);
        byte[] data = Common.GetFormat("2300", 1, 1, new String[]{"1"});
        Common.ServicesAllSend(data);
    }


    public static void SendFirst() {
        Intent intent = new Intent();
        intent.setAction("com.project.services.FirstConnect");
        ExceptionApplication.context.sendBroadcast(intent);
    }


    //发送底座
    public static byte[] GetFormat(String cmd, String type, int total, int current, String[] data) {
        StringBuffer sb = new StringBuffer();
        sb.append("@");
        sb.append(cmd);
        sb.append("," + type);
        sb.append("," + DeviceNumber);
        sb.append("," + total);
        sb.append("," + current);
        for (int i = 0; i < data.length; i++) {
            sb.append("," + data[i]);
            if (i + 1 >= data.length) {
                sb.append("$");
            }
        }
        return sb.toString().getBytes();
    }

    public static byte[] GetFormatTransparent(String cmd, String type, int total, int current, String data) {
        StringBuffer sb = new StringBuffer();
        sb.append("@");
        sb.append(cmd);
        sb.append("," + type);
        sb.append("," + DeviceNumber);
        sb.append("," + total);
        sb.append("," + current);
        sb.append(",/dev/ttyS3");
        sb.append("," + data.length());
        sb.append("," + data);
        sb.append("$");
        return sb.toString().getBytes();
    }

    //发送App
    public static byte[] GetFormat(String cmd, int total, int current, String[] data) {
        StringBuffer sb = new StringBuffer();
        sb.append("@");
        sb.append(cmd);
        sb.append("," + total);
        sb.append("," + current);
        for (int i = 0; i < data.length; i++) {
            sb.append("," + data[i]);
            if (i + 1 >= data.length) {
                sb.append("$");
            }
        }
        return sb.toString().getBytes();
    }

    public static int Xor(byte[] buffer, int start) {
        int value = 0;
        for (int i = start; i < buffer.length - 1; i++) {
            value ^= buffer[i];
        }
        return value;
    }

}
