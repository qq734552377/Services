package com.project.services.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.project.services.R;

public class MainActivity extends Activity {

    public Button button1;
    public Button button2;
    EditText ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ed = (EditText) findViewById(R.id.ed);

        Intent intent = new Intent();
        intent.setAction("StartTest");
        sendBroadcast(intent);


        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction("com.example.zxc.blue.tcpConnect");
                intent1.putExtra("SSID", "Tenda_Ucast");
                intent1.putExtra("PASSWORD", "12345678");
                intent1.putExtra("IP", "192.168.1.108");
                intent1.putExtra("PORT", "");
                sendBroadcast(intent1);
                //   Intent intent1=new Intent();
//                intent1.setAction("TCP_Connect");
//                intent1.putExtra("IP", "192.168.1.106");
                sendBroadcast(intent1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction("com.example.zxc.blue.tcpConnect");
                intent1.putExtra("SSID", "3fccd5653076d542");//HUAWEI_66b2
                intent1.putExtra("PASSWORD", "STATION_IDYL87451");//STATION_IDYL87451
                intent1.putExtra("IP", "192.168.43.1");
                intent1.putExtra("PORT", "");
                sendBroadcast(intent1);
//                PadSerialPort channel = MermoySerial.GetChannel(Common.SerialName);
//                if (channel == null)
//                    return;
//                channel.Dispose();
//                Intent intent1 = new Intent();
//                intent1.setAction("com.example.zxc.blue.tcpConnect");
//                intent1.putExtra("SSID", "Tenda_Ucast");
//                intent1.putExtra("PASSWORD", "12345678");
//                intent1.putExtra("IP", "192.168.1.125");
//                intent1.putExtra("PORT", "");
//                sendBroadcast(intent1);
            }
        });
    }

}
