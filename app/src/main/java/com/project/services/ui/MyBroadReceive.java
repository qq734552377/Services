package com.project.services.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.project.services.socket.TimerConnect.WhileCheckClient;

/**
 * Created by Administrator on 2016/2/19.
 */
public class MyBroadReceive extends BroadcastReceiver {
    public static final String action_boot = "android.intent.action.BOOT_COMPLETED";
    public static final String ConnectStr = "com.example.zxc.blue.tcpConnect";
    public static final String Test = "StartTest";
    public static final String DisConnect = "com.project.services.disConnect";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("calm", "------Servers收到广播-----" + intent.getAction());
        if (intent.getAction().equals(action_boot) || intent.getAction().equals(Test)) {
            Intent ootStartIntent = new Intent(context, MyService.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(ootStartIntent);
            return;
        }
        if (intent.getAction().equals(ConnectStr)) {
            final String info = intent.getStringExtra("IP");
            Log.e("calm", "---Ip:" + info);
            if (info == null || info == "")
                return;
            final String ssid = intent.getStringExtra("SSID");
            final String password = intent.getStringExtra("PASSWORD");

            String port = intent.getStringExtra("PORT");
            Log.e("calm", "-------ssid-------" + ssid);
            Log.e("calm", "-------password-------" + password);
            Log.e("calm", "-------port-------" + port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WhileCheckClient.Run(ssid, password, info);
                }
            }).start();


        }
//        if (intent.getAction().equals(DisConnect)) {
//            String ssid = intent.getStringExtra("SSID");
//            //DialogBuilder.getDialog("服务已经断开(断开连接的SSID:"+ ssid + "),请到底座处重连!", " ", " ");
//        }
    }


}
