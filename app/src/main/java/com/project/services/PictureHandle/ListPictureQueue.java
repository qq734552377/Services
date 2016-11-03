package com.project.services.PictureHandle;


import android.util.Log;

import com.project.services.Model.PictureModel;
import com.project.services.socket.Common;
import com.project.services.tool.SeedNmber;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.Channel;


public class ListPictureQueue {

    private static List<PictureModel> list = new ArrayList();

    private static Timer timer;

    private static boolean onOff;


    private static boolean isSendSucess=false;

    public static void StartTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (!onOff)
                    onOff = true;
                try {
                    synchronized (list) {
                        if (list.size() <= 0)
                            return;
                        PictureModel info = list.get(0);
                        long time = (long) (System.currentTimeMillis() - info.getOutTime()) / 1000;
                        if (time < 15) {
                            return;
                        }else{
                            isSendSucess=false;
                        }

                        if (isSendSucess) {
                            Remove();
                            isSendSucess = false;
                        }

                        SendPrintResult("1", info.getPath());
                        //从队列中取出图片 发送下一张图片
                        sendNext();
                    }
                    onOff = false;
                } catch (Exception e) {
                    sendNext();
                    onOff = false;
                }
            }
        }, 2000, 5000);

    }

    public static void EndTime() {
        if (timer == null)
            return;
        timer.cancel();
    }

    public static void Add(PictureModel model) {
        synchronized (list) {
            list.add(model);
        }
    }

    public static int GetCount() {
        int s = list.size();
        return s;
    }

    private static void Clean() {
        synchronized (list) {
            if (list.size() <= 0)
                return;
            list.remove(0);
        }
    }

    private static void Remove() {
        if (list.size() <= 0)
            return;
        list.remove(0);
    }

    public static void SendFirst() {
        synchronized (list) {
            if (list.size() <= 0)
                return;


            //获取队列中第一张图片
            PictureModel info = list.get(0);
            if (info == null || info.BufferPicture.size() <= 0)
                return;

            //发送第一张图片的第一包内容

            //TODO 可能在此处一次性发送全部数据  有待改进
            String seed = SeedNmber.GetNextNumber();
            StringBuffer buff=new StringBuffer();
            buff.append("@");
            buff.append("1001");
            buff.append(",");
            buff.append("1");
            buff.append(",");
            buff.append(seed);
            buff.append(",");
            buff.append(info.getTotal()+"");
            buff.append(",");

            int size=info.BufferPicture.size();
            Log.e("SendFirst", "图片的总包数为:" +info.BufferPicture.size() );
            Log.e("SendFirst", "一个总的长度为:" +info.BufferPicture.get(0).length() );

            //图片的数据
            for (int i = 0; i <size ; i++) {
                if (i==size-1){
                    buff.append(info.BufferPicture.get(i));
                }else {
                    buff.append(info.BufferPicture.get(i) + "####");
                }
            }


            Log.e("SendFirst", "buff的长度为:" + buff.toString().length());
            //TODO 可能缺一个数据的长度
            buff.append("$");
            byte[] str=buff.toString().getBytes();
            isSendSucess = Common.SendData(str);
            //如果发送成功  设置超时状态
            if (isSendSucess){
                info.setOutTime(System.currentTimeMillis());
            }
            Log.e(TAG, "SendFirst 发送结果为: " + isSendSucess);

            buff=null;
            str=null;
        }
    }

    public static void sendNext(){
        if (list.size() <= 0)
            return;


        //获取队列中第一张图片
        PictureModel info = list.get(0);
        if (info == null || info.BufferPicture.size() <= 0)
            return;

        //发送第一张图片的第一包内容

        //TODO 可能在此处一次性发送全部数据  有待改进
        String seed = SeedNmber.GetNextNumber();
        StringBuffer buff=new StringBuffer();
        buff.append("@");
        buff.append("1001");
        buff.append(",");
        buff.append("1");
        buff.append(",");
        buff.append(seed);
        buff.append(",");
        buff.append(info.getTotal()+"");
        buff.append(",");

        int size=info.BufferPicture.size();
        Log.e("sendNext", "图片的总包数为:" +info.BufferPicture.size() );
        Log.e("sendNext", "一个总的长度为:" +info.BufferPicture.get(0).length() );

        //图片的数据
        for (int i = 0; i <size ; i++) {
            if (i==size-1){
                buff.append(info.BufferPicture.get(i));
            }else {
                buff.append(info.BufferPicture.get(i) + "####");
            }
        }


        Log.e("sendNext", "buff的长度为:" + buff.toString().length());
        //TODO 可能缺一个数据的长度
        buff.append("$");
        byte[] str=buff.toString().getBytes();


        isSendSucess = Common.SendData(str);

        //如果发送成功  设置超时状态
        if (isSendSucess){
            info.setOutTime(System.currentTimeMillis());
        }

        Log.e(TAG, "sendNext 发送结果为: " + isSendSucess);
        buff=null;
        str=null;
    }


    private static final String TAG = "ListPictureQueue";

    public static void Send(int index) {
        synchronized (list) {
            ResultSend(index);
        }
    }

    private static void ResultSend(int index) {
        if (list.size() <= 0 || index < 0)
            return;
        try {
            PictureModel info = list.get(0);
            if (info.BufferPicture.size() <= 0 && isSendSucess) {
                Remove();
                return;
            }

            if (info.BufferPicture.size() <= index) {
                //TODO 发送给指定的通道,告知图片已经发送下去了,待改
                Channel channel=info.getChannel();

                SendPrintResult("0",info.getPath());


                //如果发送成功
                if (isSendSucess) {
                    Remove();
                    isSendSucess=false;
                }

                if (list.size() <= 0)
                    return;

                //TODO 改动过 8.10 15:25
                sendNext();
                return;
            }
            //发送
        } catch (Exception e) {
            if (isSendSucess) {
                Remove();
                isSendSucess=false;
            }
            //TODO 改动过 8.10 15:25
            sendNext();
        }
    }

    private static void SendPrintResult(String value,String pathName) {
        byte[] data = Common.GetFormat("2200", 1, 1, new String[]{value,pathName});
        Common.ServicesAllSend(data);
    }




    //过时方法
    @Deprecated
    public static void SendNext() {
        if (list.size() <= 0)
            return;
        PictureModel info = list.get(0);
        if (info == null || info.BufferPicture.size() <= 0)
            return;
        byte[] buffer = info.BufferPicture.get(0).getBytes();
        info.setOutTime(System.currentTimeMillis());
        Common.SendData(buffer);
    }

}
