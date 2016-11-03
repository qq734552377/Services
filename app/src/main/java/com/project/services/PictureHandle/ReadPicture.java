package com.project.services.PictureHandle;

import android.graphics.Bitmap;

import com.project.services.Model.PictureModel;
import com.project.services.socket.ArrayQueue;
import com.project.services.socket.Common;
import com.project.services.tool.SeedNmber;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/16.
 */
public class ReadPicture {

    private boolean _mDispose;

    private ArrayQueue<Bitmap> _mQueues = new ArrayQueue<Bitmap>(0x400);
    private ArrayQueue<Channel> channleQueue = new ArrayQueue<Channel>(0x400);
    private ArrayQueue<String> pathNameQueue = new ArrayQueue<String>(0x400);

    // Methods
    public ReadPicture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WRun();
            }
        }).start();
    }

    /// <summary>
    /// 添加列队
    /// </summary>
    /// <param name="iObj"></param>
    public void Add(Bitmap iObj,Channel channel,String psthName) {
        synchronized (_mQueues) {
            _mQueues.enqueue(iObj);
            channleQueue.enqueue(channel);
            pathNameQueue.enqueue(psthName);
        }
    }

    /// <summary>
    /// 释放线程
    /// </summary>
    public void Dispose() {
        if (!_mDispose) {
            _mDispose = true;
        }
    }

    private Bitmap GetItem() {
        synchronized (_mQueues) {
            if (_mQueues.size() > 0) {
                return _mQueues.dequeue();
            }
            return null;
        }
    }
    private Channel GetChannelItem() {
        synchronized (_mQueues) {
            if (channleQueue.size() > 0) {
                return channleQueue.dequeue();
            }
            return null;
        }
    }
    private String GetPathNamelItem() {
        synchronized (_mQueues) {
            if (pathNameQueue.size() > 0) {
                return pathNameQueue.dequeue();
            }
            return null;
        }
    }

    private void OnRun() {
        Bitmap item = GetItem();
        Channel channelItem=GetChannelItem();
        String pathName=GetPathNamelItem();
        try {
            if (item != null) {
                byte[] pictureByte = TurnBytes(item);//转换byte数组
                //解析获得了一张图片的完整信息PictureModel
                PictureModel info = WholeBytes(pictureByte);
                info.setChannel(channelItem);
                info.setPath(pathName);
                //接收完数据数据后 开始加入到打印队列里并发送
                if (ListPictureQueue.GetCount() <= 0) {
                    ListPictureQueue.Add(info);
                    ListPictureQueue.SendFirst();
                    return;
                }
                ListPictureQueue.Add(info);

                //释放c层的bitmap对象的内存
                item.recycle();
            } else {
                Thread.sleep(50);
            }
        } catch (Exception e) {

        }
    }

    private void WRun() {
        while (!_mDispose) {
            OnRun();
        }
    }

    private byte[] HeadBytes(int total) {
        byte[] btHead = new byte[20];
        btHead[0] = 0x42;
        btHead[1] = 0x4d;
        btHead[2] = 0x41;
        btHead[3] = 0x50;
        btHead[4] = 0x02;
        btHead[5] = 0x00;
        btHead[6] = 0x01;
        btHead[7] = 0x00;
        btHead[8] = 0x30;
        btHead[9] = 0x00;
        for (int i = 10; i < 20; i++) {
            btHead[i] = 0x00;
        }
        btHead[4] = (byte) (total & 0Xff);
        btHead[5] = (byte) ((total & 0Xff00) >> 8);
        return btHead;

    }

    private int PackageTotal(int data_size) {
        return data_size % 960 == 0 ? data_size / 960 : (data_size / 960) + 1;
    }

    private PictureModel WholeBytes(byte[] btData) {

        int package_total = PackageTotal(btData.length); //获取总包数


        String seed = SeedNmber.GetNextNumber();
        byte[] btHead = HeadBytes(package_total);//包头
        PictureModel model = new PictureModel();
        int sum = btData.length % 960;
        byte[] sum_L_H = new byte[2];
        sum_L_H[0] = (byte) (sum & 0Xff);
        sum_L_H[1] = (byte) ((sum & 0Xff00) >> 8);
        int t = 0;
        for (t = 0; t < btData.length / 960; t++) {
            byte[] content_senf = join(btHead, content_send_data(btData, t * 960, 960));
            content_senf[6] = (byte) ((t + 1) & 0Xff);
            content_senf[7] = (byte) (((t + 1) & 0Xff00) >> 8);
            content_senf[8] = (byte) 0xc0;
            content_senf[9] = 0x03;
//            Log.e("PackageTotal",  + content_senf[6]+"位置"+content_senf[7]);
            String encode_str = Common.encode(content_senf);//编码
            String str = getPackageString("1001", "1", seed, package_total, t, encode_str.length(), encode_str);
            //TODO 这有改动8.9 15:39
//            model.BufferPicture.add(str.getBytes());
            model.BufferPicture.add(encode_str);
        }
        if (btData.length % 960 != 0) {
            byte[] content_senf = join(btHead, content_send_data(btData, t * 960, sum));
            content_senf[6] = (byte) ((t + 1) & 0Xff);
            content_senf[7] = (byte) (((t + 1) & 0Xff00) >> 8);
            content_senf[8] = sum_L_H[0];
            content_senf[9] = sum_L_H[1];

            String encode_str = Common.encode(content_senf);//编码
            String str = getPackageString("1001", "1", seed, package_total, t, encode_str.length(), encode_str);

//            model.BufferPicture.add(str.getBytes());
            //TODO 这有改动8.9 15:39
            model.BufferPicture.add(encode_str);
        }


        model.setOutTime(System.currentTimeMillis());
        model.setTotal(package_total);
        return model;
    }

    private byte[] join(byte[] a1, byte[] a2) {
        byte[] result = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, result, 0, a1.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;

    }

    // 图片分包
    private byte[] content_send_data(byte[] by, int start, int size) {
        byte[] send = new byte[size];
        System.arraycopy(by, start, send, 0, size);
        return send;
    }

    //图片base64之后包装
    private String getPackageString(String cmd, String type, String number, int total, int serial, int len, String data) {
        return "@" + cmd + "," + type + "," + number + "," + total + "," + serial + "," + len + "," + data + "$";
    }

    private byte[] TurnBytes(Bitmap bitmap) {
        int W = bitmap.getWidth();
        int H = bitmap.getHeight();
//        Log.e("ReadPicture.java","图片的长度为:"+W+"图片的宽度为:"+H);

        byte[] bt = new byte[W / 8 * H];
        int idx = 0;
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j = j + 8) {
                byte value = 0;
                for (int s = 0; s <= 7; s++) {
                    int a = bitmap.getPixel(j + s, i);
                    int aa = a & 0xff;
                    if (aa != 255) {
                        value |= 1 << s;
                    }
                }
                bt[idx] = value;
                idx++;
            }
        }
        return bt;
    }
}
