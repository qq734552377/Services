package com.project.services.socket.Message;

public class SerialDataSend extends MessageBase {

    public int Total;
    public int CurrentPackt;
    public String Data;

    public void Load(String[] str) {
        super.Load(str);

            Cmd = str[0];
            //System.out.println(str[1]);
            Total = Integer.parseInt(str[1]);
            CurrentPackt = Integer.parseInt(str[2]);
            Data = str[3];

    }
}
