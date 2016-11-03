package com.project.services.serial;

import android.util.Log;

import com.project.services.socket.Common;
import com.project.services.socket.Memory.NettyClientMap;
import com.project.services.socket.NioTcpClient;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/6/6.
 */
public class Restart {
    private static Object obj = new Object();

    private static Timer timer;

    private static boolean restart;

    public static void StartTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (obj) {
                    try {
                        if (!restart)
                            return;
                        MermoySerial.Remove(Common.SerialName);
                        PadSerialPort padSerialPort = new PadSerialPort("/dev/ttyS2", 115200);
                        padSerialPort.Open();
                        MermoySerial.Add(padSerialPort);
                        restart = false;
                    } catch (Exception e) {
                        restart = false;
                    }
                }
            }
        }, 2000, 4000);
    }

    public static void Check() {
        synchronized (obj) {
            restart = true;
        }
    }
}
