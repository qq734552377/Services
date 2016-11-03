package com.project.services.socket.TimerConnect;

import android.util.Log;

import com.project.services.socket.Common;
import com.project.services.socket.Memory.NettyClientMap;
import com.project.services.socket.NioTcpClient;
import com.project.services.tool.ExceptionApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/4/21.
 */
public class WhileCheckClient {

    private static Object obj = new Object();

    private static Timer timer;

    private static int TotalNumber = 1;

    private static long heartTime;

    public static void StartTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (obj) {
                    NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
                    if (client == null)
                        return;
                    long second = (System.currentTimeMillis() - heartTime) / 1000;
                    if (second >= 12) {
                        client.Dispose();
                    } else {
                        boolean result = WhetherCconnect(client);
                        if (result)
                            return;
                        ExceptionApplication.gLogger.info("Client No Ip:" + client.Ip + " Ssid:" + client.SSid);
                    }
                    NettyClientMap.Remove(Common.ConfigKey);
//                    if (TotalNumber > 2) {
//                        Common.SendStop(client.SSid);
//                        return;
//                    }
                    TotalNumber++;
                    ClinetRun(client.SSid, client.Password, client.Ip, false);
                }
            }
        }, 3000, 3000);
    }


    public static void HeartbeatTimeUpdate() {
        synchronized (obj) {
            heartTime = System.currentTimeMillis();
            System.out.println("------心跳发送--回来了------");
        }
    }

    public static void UpTotalNumber() {
        synchronized (obj) {
            TotalNumber = 1;
        }
    }

    private static boolean WhetherCconnect(NioTcpClient client) {
        if (client.WaitChannel == 0) {
            return true;
        } else if (client.WaitChannel == 1) {
            boolean success = client.f.isSuccess();
            SendHead();
            System.out.println("------心跳发送--------");
            return true;
        }
        Log.e("calm", "Heartbeat send connection error " + client.Ip + " Ssid:" + client.SSid);
        return false;
    }

    public static void Run(String ssid, String password, String ip) {
        synchronized (obj) {
            NioTcpClient client = NettyClientMap.GetChannel(Common.ConfigKey);
            if (client == null) {
                TotalNumber = 1;
                ClinetRun(ssid, password, ip, true);
                ExceptionApplication.gLogger.info("First Connect Ip:" + ip + " Ssid:" + ssid);
                return;
            }
            if (client.WaitChannel == 2 || client.WaitChannel == 0) {
                client.Dispose();
                Log.e("calm", "Being and connection error " + ip + " Ssid:" + ssid);
                NettyClientMap.Remove(Common.ConfigKey);
                TotalNumber = 1;
                ClinetRun(ssid, password, ip, true);
                ExceptionApplication.gLogger.info("Reset Connect Ip:" + ip + " Ssid:" + ssid);
                return;
            }
            if (ssid.equals(client.SSid) && ip.equals(ip)) {
                if (client.Old) {
                    Log.e("calm", "Old Connection close " + ip + " Ssid:" + ssid);
                    return;
                }

                //TODO 9.14改
//                Common.SendUpdate();
                ExceptionApplication.gLogger.info("Same Connect Ip:" + ip + " Ssid:" + ssid);
                return;
            }
            client.Dispose();
            Log.e("calm", "Connection close " + ip + " Ssid:" + ssid);
            NettyClientMap.Remove(Common.ConfigKey);
            TotalNumber = 1;
            ClinetRun(ssid, password, ip, true);

        }
    }

    private static void ClinetRun(String ssid, String password, String ip, boolean old) {
        NioTcpClient clientFor = new NioTcpClient(ssid, password, ip, 43708, old);
        heartTime = System.currentTimeMillis();
        NettyClientMap.Add(clientFor);
        new Thread(clientFor).start();
    }

    private static void SendHead() {
        String heart = "@1105,123456789$";
        Common.SendDataHead(heart.getBytes());
    }
}
