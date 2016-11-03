package com.project.services.socket.MessageCallback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.project.services.PictureHandle.ListPictureQueue;
import com.project.services.PictureHandle.ReadPicture;
import com.project.services.PictureHandle.ReadPictureManage;
import com.project.services.socket.Common;
import com.project.services.socket.Memory.NettyChannelMap;
import com.project.services.socket.Memory.NettyClientMap;
import com.project.services.socket.Message.Heartbeat;
import com.project.services.socket.Message.IcPass;
import com.project.services.socket.Message.MessageBase;
import com.project.services.socket.Message.MoneyBox;
import com.project.services.socket.Message.NfcPass;
import com.project.services.socket.Message.PrintMessage;
import com.project.services.socket.Message.PrintState;
import com.project.services.socket.Message.PrintStateReply;
import com.project.services.socket.Message.ReadNfc;
import com.project.services.socket.Message.ReadTwoCode;
import com.project.services.socket.Message.ReceiveSerialData;
import com.project.services.socket.Message.ReceiveUsbData;
import com.project.services.socket.Message.SerialDataSend;
import com.project.services.socket.Message.SerialSetting;
import com.project.services.socket.Message.SerialSettingReply;
import com.project.services.socket.Message.SerivalDataCallback;
import com.project.services.socket.Message.StationPrintMessage;
import com.project.services.socket.Message.TwoCodePass;
import com.project.services.socket.NioTcpClient;
import com.project.services.tool.ExceptionApplication;

import java.io.File;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/4.
 */
public class CallbackHandle implements IMsgCallback {
    @Override
    public void Receive(Channel channel, Object obj) {
        if (obj == null)
            return;
        if (!(obj instanceof MessageBase))
            return;
        try {

            FindHandle(channel, obj);
        } catch (Exception e) {

        }
    }

    private static final String TAG = "CallbackHandle";
    public void FindHandle(Channel channel, Object msg) {
        try {

            if (msg instanceof SerivalDataCallback) {
                SerivalDataCallback info = (SerivalDataCallback) msg;
                if (info == null)
                    return;

//                Log.e(TAG, "FindHandle 回来");

                ExceptionApplication.gLogger.info("SerivalDataCallback : send deblue sucess " + true );
                byte[] appdata = Common.GetFormat("1201", 1, 1, new String[]{info.Result ? "0" : "1"});
                Common.ServicesAllSend(appdata);
                return;
            }


            if (msg instanceof Heartbeat) {
                Heartbeat heartbeat = (Heartbeat) msg;
                if (heartbeat == null)
                    return;
                BindInfo(channel, heartbeat.data);
            }

            if (msg instanceof StationPrintMessage) {
                StationPrintMessage info = (StationPrintMessage) msg;
                if (info == null)
                    return;
                ListPictureQueue.Send(info.CurrentPackt + 1);
                ExceptionApplication.gLogger.info("StationPrintMessage : deblue get picture and service will send next picture which in the ListPictureQueue");
                return;
            }
            if (msg instanceof SerialSettingReply) {
                SerialSettingReply info = (SerialSettingReply) msg;
                if (info == null)
                    return;
                String result = info.Reulst.equals("true") ? "0" : "1";
                byte[] data = Common.GetFormat("1200", 1, 1, new String[]{"1", result});
                Common.ServicesAllSend(data);
                return;
            }
            if (msg instanceof ReceiveSerialData) {
                ReceiveSerialData info = (ReceiveSerialData) msg;
                if (info == null)
                    return;
                byte[] appdata = Common.GetFormat("1202", info.Total, info.CurrentPackt, new String[]{info.Data});
                Common.ServicesAllSend(appdata);
                return;
            }
            if (msg instanceof ReceiveUsbData) {
                ReceiveUsbData info = (ReceiveUsbData) msg;
                if (info == null)
                    return;
                byte[] appdata = Common.GetFormat("2201", info.Total, info.CurrentPackt, new String[]{info.Data});
                Common.ServicesAllSend(appdata);
                return;
            }
            if (msg instanceof PrintStateReply) {
                PrintStateReply info = (PrintStateReply) msg;
                if (info == null)
                    return;
                byte[] data = Common.GetFormat("1204", 1, 1, new String[]{"1", info.paper, info.temp});
                Common.ServicesAllSend(data);
                return;
            }

            Channel channelcode = NettyChannelMap.GetChannel(channel.id().toString());
            if (channelcode == null)
                return;
            if (msg instanceof PrintMessage) {
                PrimeHandle(channel, (PrintMessage) msg);
                return;
            }

            if (msg instanceof SerialSetting) {
                SerialSetting info = (SerialSetting) msg;
                if (info == null)
                    return;
                byte[] data = Common.GetFormat("1100", "1", 1, 1, new String[]{"/dev/ttyS3", info.Baud});
                //Common.SendData(data);
                boolean result = Common.SendData(data);
                if (result)
                    return;
                //result?"1":"2";
                byte[] dataBuffer = Common.GetFormat("1200", 1, 1, new String[]{"2", "0"});
                Common.ChannelSendBuffer(channel, dataBuffer);
                return;
            }

            if (msg instanceof SerialDataSend) {
                SerialDataSend info = (SerialDataSend) msg;
                if (info == null)
                    return;
                byte[] data = Common.GetFormatTransparent("1101", "1", 1, 1, info.Data);
                boolean result = Common.SendData(data);
                ExceptionApplication.gLogger.info("SerialDataSend :" + result + "  data: " + info.Data);
//                byte[] appdata = Common.GetFormat("1201", 1, 1, new String[]{result ? "0" : "1"});
//                Common.ChannelSendBuffer(channel, appdata);
                return;
            }

            if (msg instanceof MoneyBox) {
                MoneyBox info = (MoneyBox) msg;
                if (info == null)
                    return;
                byte[] data = Common.GetFormat("1103", "1", 1, 1, new String[]{info.state});
                Common.SendData(data);
                return;
            }
            if (msg instanceof PrintState) {
                PrintState info = (PrintState) msg;
                if (info == null)
                    return;
                byte[] data = Common.GetFormat("1104", "1", 1, 1, new String[]{info.data});
                boolean result = Common.SendData(data);
                if (result)
                    return;
                byte[] dataBuffer = Common.GetFormat("1204", 1, 1, new String[]{"2", "N", "1"});
                Common.ChannelSendBuffer(channel, dataBuffer);
                return;
            }

            if (msg instanceof ReadTwoCode) {
                ReadTwoCode info = (ReadTwoCode) msg;
                if (info == null)
                    return;
                if (!info.data.trim().equals("1"))
                    return;
                byte[] buffer = new byte[]{'@', 'S', 'C', 'A', '0', '0', '0', '9', 0x7E, 0x00, 0x08, 0x01, 0x00, 0x02, 0x01, 0x00, 0x00};
                Common.SendSerial(buffer);
                return;
            }
            if (msg instanceof TwoCodePass) {
                TwoCodePass info = (TwoCodePass) msg;
                if (info == null)
                    return;
                Common.SendSerial(Common.decode(info.data));
                return;
            }
            if (msg instanceof ReadNfc) {
                ReadNfc info = (ReadNfc) msg;
                if (info == null)
                    return;
                if (!info.data.trim().equals("1"))
                    return;
                byte[] buffer01 = new byte[]{0x40, 0x52, 0x46, 0x30, 0x30, 0x30, 0x30, 0x36, 0x06, 0x01, 0x41, 0x00, (byte) 0xB9, 0x03};
                Common.SendSerial(buffer01);
                byte[] buffer = new byte[]{0x40, 0x52, 0x46, 0x30, 0x30, 0x30, 0x30, 0x36, 0x06, 0x01, 0x42, 0x00, (byte) 0xBA, 0x03};
                Common.SendSerial(buffer);
                byte[] buffer2 = new byte[]{0x40, 0x52, 0x46, 0x30, 0x30, 0x30, 0x30, 0x37, 0x07, 0x02, 0x41, 0x01, 0x52, (byte) 0xE8, 0x03};
                Common.SendSerial(buffer2);
                return;
            }
            if (msg instanceof NfcPass) {
                NfcPass info = (NfcPass) msg;
                if (info == null)
                    return;
                Common.SendSerial(Common.decode(info.data));
                return;
            }

            if (msg instanceof IcPass) {
                IcPass info = (IcPass) msg;
                if (info == null)
                    return;
                Common.SendSerial(Common.decode(info.data));
                return;
            }







        } catch (Exception e) {

        }
    }

    private void PrimeHandle(Channel channel, PrintMessage msg) {
        try {

            Common.CheckFirst();

            NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
            if (client == null || !client.ClietnStatus()) {
                byte[] data = Common.GetFormat("2300", 1, 1, new String[]{"2"});
                Common.ChannelSendBuffer(channel, data);
                return;
            }
            String path = msg.Data;
            File file = new File(path);
            if (!file.exists()) {
                //回复错误
                return;
            }
            String filename = file.getName();
            int index = filename.lastIndexOf(".");
            if (index <= -1) {
                //回复错误
                return;
            }
            String format = filename.substring(index + 1, filename.length());
            if (!format.toUpperCase().equals("BMP")) {
                //回复错误
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(path);


            if (bitmap == null) {
                ExceptionApplication.gLogger.info("bitmap Data Null:" + path);
                return;
            }



            if (bitmap.getWidth() != 384) {
                return;
            }

            ExceptionApplication.gLogger.info("Data File Image Path Name:" + path);
            ExceptionApplication.gLogger.info("Data File Image Channel:" + channel.id().toString());


            ReadPicture picture = ReadPictureManage.GetInstance().GetReadPicture(0);
            if (picture == null)
                return;

            //TODO 需要加上channel参数以便后来确认发送返回信息 加入了图片名字
            picture.Add(bitmap,channel,filename);
        } catch (Exception e) {

        }


    }

//    private void PrimeHandle(Channel channel, PrintMessage msg) {
//        try {
//            NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
//            if (client == null || !client.ClietnStatus()) {
//                byte[] data = Common.GetFormat("2300", 1, 1, new String[]{"2", " "});
//                Common.ChannelSendBuffer(channel, data);
//                return;
//            }
//            String path = msg.Data;
//            File file = new File(path);
//            if (!file.exists()) {
//                //回复错误
//                return;
//            }
//            String filename = file.getName();
//            int index = filename.lastIndexOf(".");
//            if (index <= -1) {
//                //回复错误
//                return;
//            }
//            String format = filename.substring(index + 1, filename.length());
//            if (!format.toUpperCase().equals("BMP")) {
//                //回复错误
//                return;
//            }
//
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            if (bitmap == null) {
////                ExceptionApplication.gLogger.info("bitmap Data Null:" + path);
//                return;
//            }
//            if (bitmap.getWidth() != 384) {
//                return;
//            }
//
////            ExceptionApplication.gLogger.info("Data File Path Name:" + path);
////            ExceptionApplication.gLogger.info("Data File Image Channel:" + channel.id().toString());
//            PictureDataChannel pictureData = new PictureDataChannel();
//            pictureData.setBitmap(bitmap);
//            pictureData.setChannelId(channel.id().toString());
//            ReadPicture picture = ReadPictureManage.GetInstance().GetReadPicture(0);
//            if (picture == null)
//                return;
//            picture.Add(pictureData,channel);
//        } catch (Exception e) {
//
//        }
//
//
//    }


    private void BindInfo(Channel channel, String dataStr) {
        byte[] buffer = Common.decode(dataStr);
        if (buffer == null) {
            channel.close();
            return;
        }
        String str = new String(buffer);
        if (!str.equals("Version.1.0")) {
            channel.close();
            return;
        }
        Channel channels = NettyChannelMap.GetChannel(channel.id().toString());
        if (channels == null) {
            NettyChannelMap.Add(channel);
            Common.SendChannelData(channel);
        } else {
            Common.SendChannelData(channels);
        }

    }


}
