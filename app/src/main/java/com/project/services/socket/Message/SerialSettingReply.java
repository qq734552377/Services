package com.project.services.socket.Message;

/**
 * Created by Administrator on 2016/2/19.
 */
public class SerialSettingReply extends MessageBase {

    public int Total;
    public int CurrentPackt;
    public int Type;
    public String Number;
    public String SerialName;
    public String Reulst;

    public void Load(String[] str) {
        super.Load(str);

            Cmd = str[0];
            Type = Integer.parseInt(str[1]);
            Number = str[2];
            Total = Integer.parseInt(str[3]);
            CurrentPackt = Integer.parseInt(str[4]);
            SerialName = str[5];
            Reulst = str[6];

    }
}
