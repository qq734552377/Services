package com.project.services.socket.MessageProtocol;

import com.project.services.socket.Message.Heartbeat;
import com.project.services.socket.Message.IcPass;
import com.project.services.socket.Message.MessageBase;
import com.project.services.socket.Message.MoneyBox;
import com.project.services.socket.Message.NfcPass;
import com.project.services.socket.Message.PrintMessage;
import com.project.services.socket.Message.PrintState;
import com.project.services.socket.Message.ReadNfc;
import com.project.services.socket.Message.ReadTwoCode;
import com.project.services.socket.Message.SerialDataSend;
import com.project.services.socket.Message.SerialSetting;
import com.project.services.socket.Message.TwoCodePass;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/17.
 */
public class ServicesPackage extends Package {
    private StringBuffer sBuffer;

    public ServicesPackage(Channel _channel) {
        super(_channel);
        sBuffer = new StringBuffer();
    }

    @Override
    public void Import(byte[] buffer, int Offset, int count) throws Exception {
        sBuffer.append(new String(buffer));
        int offset = 0;
        while (sBuffer.length() > offset && !mDispose) {
            int startIndex = sBuffer.indexOf("@", offset);
            if (startIndex == -1)
                break;
            int endIndex = sBuffer.indexOf("$", startIndex);
            if (endIndex == -1)
                break;
            int len = endIndex + 1;
            String value = sBuffer.substring(startIndex, len);
            OnMessageDataReader(value);
            offset = len;
        }
        sBuffer.delete(0, offset);
    }

    @Override
    public MessageBase MessageRead(byte[] data) {
        return null;
    }

    public MessageBase MessageRead(String value) throws Exception {
        String msg = value.substring(1, value.length() - 1);
        String[] item = msg.split(",");
        MessageBase mbase = null;
        switch (item[0]) {
            case "1101":
                mbase = new SerialDataSend();
                break;
            case "2100":
                mbase = new PrintMessage();
                break;
            case "1100":
                mbase = new SerialSetting();
                break;
            case "1103":
                mbase = new MoneyBox();
                break;
            case "1104":
                mbase = new PrintState();
                break;
            //读取二维码
            case "1105":
                mbase = new ReadTwoCode();
                break;
            //二维码透传
            case "1106":
                mbase = new TwoCodePass();
                break;
            //IC卡透传
            case "1107":
                mbase = new IcPass();
                break;
            //NFC透传
            case "1108":
                mbase = new NfcPass();
                break;
            case "1109":
                mbase = new ReadNfc();
                break;
            case "1110":
                mbase = new Heartbeat();
                break;

        }
        if (mbase == null)
            return null;

        try {
            mbase.Load(item);
        } catch (Exception e) {
            return null;
        }
        return mbase;
    }
}
