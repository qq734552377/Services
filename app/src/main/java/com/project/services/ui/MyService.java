package com.project.services.ui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.project.services.PictureHandle.ListPictureQueue;
import com.project.services.serial.MermoySerial;
import com.project.services.serial.PadSerialPort;
import com.project.services.serial.Restart;
import com.project.services.socket.NioTcpServer;
import com.project.services.socket.TimerConnect.WhileCheckClient;
import com.project.services.tool.SeedNmber;

public class MyService extends Service {
    public NioTcpServer tcpServer;

    public MyService()
    {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        SeedNmber.InitSeed();
        WhileCheckClient.StartTimer();

        PadSerialPort padSerialPort = new PadSerialPort("/dev/ttyS2", 115200);
        padSerialPort.Open();
        MermoySerial.Add(padSerialPort);

        Restart.StartTimer();
        if (tcpServer == null) {
            tcpServer = new NioTcpServer(7070);
            new Thread(tcpServer).start();//开启TCP服务
            ListPictureQueue.StartTimer();//图片处理列队

        } else {
            return;
        }

    }

}
