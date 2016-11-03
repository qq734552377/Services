package com.project.services.socket.Message;

/**
 * Created by Administrator on 2016/2/19.
 */
public class SerialSetting extends MessageBase {
    public int Total;
    public int CurrentPackt;
    public String Baud;

    public void Load(String[] str) {
        super.Load(str);
            Cmd = str[0];
            Total = Integer.parseInt(str[1]);
            CurrentPackt = Integer.parseInt(str[2]);
            Baud = str[3];
    }
}
