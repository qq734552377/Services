package com.project.services.serial;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.project.services.socket.ArrayQueue;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/6/2.
 */
public class PadSerialPort {
    private SerialPort ser;
    private InputStream intput;
    private OutputStream output;
    private int Baudrate = 115200;
    private String Path = "/dev/ttyS2";
    public String Name = "ttyS2";
    private boolean mDispose;
    private byte[] Buffer;
    private int Offset;
    private String str = "";
    private ArrayQueue<byte[]> _mQueues = new ArrayQueue<byte[]>(0x400);

    private MessagerHandler msgHandler;

    public PadSerialPort(String path, int baudrate) {
        Baudrate = baudrate;
        Path = path;
        Buffer = new byte[2048];
        Offset = 0;
        msgHandler = new MessagerHandler();
    }

    public boolean Open() {
        try {
            ser = new SerialPort(new File(Path), Baudrate, 0);
            intput = ser.getInputStream();
            output = ser.getOutputStream();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Receive();
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WRun();
                }
            }).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendConfigQRcode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[]{'@', 'L', 'E', 'D', '0', '0', '0', '1', '1'};
                SendMessage(buffer);
                byte[] buffer0 = new byte[]{'@', 'S', 'C', '1', '0', '0', '0', '0'};
                byte[] buffer01 = new byte[]{'@', 'S', 'C', 'A', '0', '0', '0', '9', 0X7E, 0X00, 0X08, 0X01, 0X00, (byte) 0XD9, (byte) 0X81, (byte) 0X6A, (byte) 0XEC};
                byte[] buffer02 = new byte[]{'@', 'S', 'C', 'A', '0', '0', '0', '9', 0X7E, 0X00, 0X08, 0X01, 0X00, 0X00, (byte) 0XD5, (byte) 0XEF, 0X41};
                byte[] buffer03 = new byte[]{'@', 'S', 'C', 'A', '0', '0', '0', '9', 0X7E, 0X00, 0X08, 0X01, 0X00, 0X06, 0X00, (byte) 0Xde, 0X3f};
                // byte[] buffer03 = new byte[]{'@', 'S', 'C', 'A', '0', '0', '0', '9', 0X7E, 0X00, 0X08, 0X01, 0X00, 0X06, (byte)0xFF, (byte) 0xC0, (byte)0xCF};

                SendMessage(buffer0);
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SendMessage(buffer01);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SendMessage(buffer02);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SendMessage(buffer03);

            }
        }).start();

    }

    /**
     * 监听串口程序
     */
    private void Receive() {
        byte[] buffer = new byte[1024];
        sendConfigQRcode();
        while (!mDispose) {
            try {
                int tatal = intput.available();
                if (tatal <= 0) {
                    Thread.sleep(15);
                    continue;
                }
                Thread.sleep(100);
                int len = intput.read(buffer);
                if (len > 0) {
                    AnalyticalProtocol(buffer, 0, len);
                    continue;
                }
                if (len < 0) {
                    Dispose();
                }
            } catch (Exception e) {
                //TODO：接收异常 应该重启
                e.printStackTrace();
                Log.e("calm", "---线程停止---");
                Dispose();
            }
        }
    }

    private void AnalyticalProtocol(byte[] buffer, int offset, int count) {
        System.arraycopy(buffer, offset, Buffer, Offset, count);
        int tmpcount = count + Offset;
        //System.out.println("Offset:" + Offset+" ReadTotal:" + count + " Sum:" + (Offset + count) + " Total:" + tmpcount);
        int pos = 0;
        while (tmpcount > 0 && !mDispose) {
            int messagelen = ParseMessage(Buffer, pos, tmpcount);
            if (messagelen == -1) {
                System.arraycopy(Buffer, pos, Buffer, 0, tmpcount);
                Offset = tmpcount;
                break;
            }
            tmpcount -= messagelen;
            pos += messagelen;
            if (tmpcount <= 0) {
                Offset = 0;
                break;
            }
        }
        //System.out.println("End:" + Offset);
    }

    private int StartIndex(byte[] buffer, int offset, int cout) {
        for (int i = offset; i < cout; i++) {
            if (buffer[i] == '@') {
                return i;
            }
        }
        return -1;
    }


    private void Send(byte[] buffer) {
        try {
            if (mDispose)
                return;
            output.write(buffer);
            output.flush();
        } catch (IOException e) {
            Dispose();
        }
    }

    private void OnRun() {
        byte[] item = GetItem();
        try {
            if (item != null) {
                Send(item);
            } else {
                Thread.sleep(50);
            }
        } catch (Exception e) {

        }
    }

    private void WRun() {
        while (!mDispose) {
            OnRun();
        }
    }

    public void AddHandle(byte[] buffer) {
        synchronized (_mQueues) {
            _mQueues.enqueue(buffer);
        }
    }

    private byte[] GetItem() {
        synchronized (_mQueues) {
            if (_mQueues.size() > 0) {
                return _mQueues.dequeue();
            }
            return null;
        }
    }

    public void SendMessage(byte[] sBuffer) {
        try {
            AddHandle(sBuffer);
        } catch (Exception e) {
        }
    }


    //关闭
    public void Dispose() {
        synchronized (this) {
            if (!mDispose) {
                mDispose = true;
                Log.e("calm", "----调用----");
                MyDispose();
                Restart.Check();
            }
        }
    }

    private void MyDispose() {
        try {
            if (intput != null) {
                intput.close();
            }
            if (output != null) {
                output.close();
            }
            ser.closeSerialPort();
        } catch (IOException e) {

        } finally {

        }
    }

    private void serial(byte[] buffer, int start, int end) {
        try {
            String head = new String(buffer, 1, 3);
            int len = Len(buffer[4], buffer[5], buffer[6], buffer[7]);
            synchronized (this) {
                if (mDispose || msgHandler == null)
                    return;
                byte[] data = new byte[len];
                System.arraycopy(buffer, 8, data, 0, len);
                //Messager(head, data);
                MessagerJoin(head, data,start,end);
            }
        } catch (Exception e) {

        }
    }

    private int Len(byte buffer1, byte buffer2, byte buffer3, byte buffer4) {
        return Integer.parseInt(String.format("%c", buffer1) + String.format("%c", buffer2) + String.format("%c", buffer3) + String.format("%c", buffer4));
    }

    private void Messager(String msg, byte[] buffer) {
        switch (msg) {
            case "MAG":
                msgHandler.MagneticCardMessager(buffer);
                break;
            case "IC0":
                msgHandler.IcCardMessager(buffer);
                break;
            case "SCA":
                msgHandler.QRCardMessager(buffer);
                break;
            case "RF0":
                msgHandler.NFCCarMessager(buffer);
                break;
        }
    }



    private void MessagerJoin(String msg, byte[] buffer, int index, int total) {
        if (msg.equals("MAG")) {//判断是否时MAG的数据,如果是
            str += new String(buffer);//把T1 T2 T3 累加
            if (index < total)
                return;
            Messager(msg, (str + "\r\n").getBytes());//判断是否时最后的数据，如果是最后的数据 发送MAG，如果不是代表后面还有数据，有可能是MAG
            str = "";
            return;
        }
        if (!str.equals("")) {//判断是否是空是否不是空，代表前面有MAG 的数据，并且这条数据已经不时MAG了
            Messager("MAG", (str + "\r\n").getBytes());//把内存中的MAG 数据发送出去，并且发送当前解析的数据
            str = "";
            Messager(msg, buffer);
        } else {
            Messager(msg, buffer);//当表前面已经没有MAG 的数据了
        }
    }


    private int ParseMessage(byte[] buffer, int offSet, int count) { //30 -15
        int bufferCount = offSet + count;
        int index = StartIndex(buffer, offSet, bufferCount);//查找头字段
        if (index == -1)//等于-1没有找到头，继续等待
            return -1;
        int len = index + 4;//当前@的位置1个字节加命令3个字节
        if (bufferCount < len + 4)
            return -1;
        int value = -1;
        try {
            value = Len(buffer[len], buffer[len + 1], buffer[len + 2], buffer[len + 3]);//长度
        } catch (Exception e) {
            return index > offSet ? (index - offSet) + 1 : 1;
        }
        int total = len + 4 + value;//前面8个字节@MAG,长度,数据
        if (bufferCount < total)
            return -1;
        int byteLen = 8 + value;//前面8个字节,(头长度,数据)
        byte[] singlebuffer = new byte[byteLen];
        System.arraycopy(buffer, index, singlebuffer, 0, byteLen);//offSet
        int rLen = index > offSet ? byteLen + (index - offSet) : byteLen;
        serial(singlebuffer, total, bufferCount);
        return rLen;
    }
}
