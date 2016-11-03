package com.project.services.socket.Message;

/**
 * Created by Administrator on 2016/8/18.
 */
public class SerivalDataCallback extends MessageBase {

    public boolean Result;

    public void Load(String[] str) {
        super.Load(str);
        Cmd = str[0];
        Result = str[2].equals("0") ? true : false;
    }
}
