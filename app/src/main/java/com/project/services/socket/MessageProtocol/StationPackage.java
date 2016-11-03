package com.project.services.socket.MessageProtocol;

import com.project.services.socket.Message.MessageBase;
import com.project.services.socket.Message.PrintStateReply;
import com.project.services.socket.Message.ReceiveSerialData;
import com.project.services.socket.Message.ReceiveUsbData;
import com.project.services.socket.Message.SerialSettingReply;
import com.project.services.socket.Message.SerivalDataCallback;
import com.project.services.socket.Message.StationPrintMessage;
import com.project.services.socket.TimerConnect.WhileCheckClient;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/3.
 */
public class StationPackage extends Package {

    private StringBuffer sBuffer;

    public StationPackage(Channel _channel) {
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
        try {
            String msg = value.substring(1, value.length() - 1);
            String[] item = msg.split(",");
            MessageBase mbase = null;
            switch (item[0]) {
                case "1000":
                    mbase = new StationPrintMessage();
                    break;
                case "1200":
                    mbase = new SerialSettingReply();
                    break;
                case "1201":
                    mbase = new ReceiveSerialData();
                    break;
                case "1002":
                    mbase = new ReceiveUsbData();
                    break;
                case "1004":
                    mbase = new PrintStateReply();
                    break;
//            case "1005"://(心跳包不作处理)
//                WhileCheckClient.HeartbeatTimeUpdate();
//                break;
                case "1006":
                    mbase = new SerivalDataCallback();
                    break;
                default:
                    break;

            }
            WhileCheckClient.HeartbeatTimeUpdate();
            if (mbase == null)
                return null;
            mbase.Load(item);
            return mbase;
        } catch (Exception e) {
            return null;
        }
    }

}
