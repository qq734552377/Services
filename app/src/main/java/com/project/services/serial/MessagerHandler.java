package com.project.services.serial;

import com.project.services.socket.Common;

/**
 * Created by Administrator on 2016/6/2.
 */
public class MessagerHandler {


    public void MagneticCardMessager(byte[] buffer) {//int start, int end
        byte[] appdata = Common.GetFormat("2301", 1, 1, new String[]{Common.encode(buffer)});
        Common.ServicesAllSend(appdata);
    }
    public void IcCardMessager(byte[] buffer) {
        if ((buffer[0] & 0xff) != 0xaa)
            return;
        int xor = Common.Xor(buffer, 1);
        //透传
        if (xor != buffer[buffer.length - 1])
            return;
        int len = (buffer[1] & 0xff) + ((buffer[2] & 0xff) << 8);
        if (buffer[3] == 0 && buffer[4] == 3) {
            byte[] appdata = Common.GetFormat("2303", 1, 1, new String[]{"1"});
            Common.ServicesAllSend(appdata);
            byte[] buffer1 = new byte[]{0x40, 0x49, 0x43, 0x30, 0x30, 0x30, 0x30, 0x37, (byte) 0xaa, 0x04, 0x00, 0x20, 0x00, 0x10, 0x34};
            Common.SendSerial(buffer1);
            return;
        }
        if (buffer[3] == 0 && buffer[4] == 4) {
            byte[] appdata = Common.GetFormat("2303", 1, 1, new String[]{"2"});
            Common.ServicesAllSend(appdata);
            return;
        }
        byte[] appdata = Common.GetFormat("2304", 1, 1, new String[]{Common.encode(buffer)});
        Common.ServicesAllSend(appdata);

    }

    public void QRCardMessager(byte[] buffer) {
        byte[] matching = new byte[]{0x02, 0x00, 0x00, 0x01, 0x00, 0x33, 0x31};
        if (getDataChar(buffer, matching)) {
            //下发成功
            return;
        }
        byte[] by = {'@', 'B', 'E', 'P', '0', '0', '0', '0'};
        Common.SendSerial(by);
        byte[] appdata = Common.GetFormat("2302", 1, 1, new String[]{Common.encode(buffer)});
        Common.ServicesAllSend(appdata);
    }


    public void NFCCarMessager(byte[] buffer) {
        int framelen = buffer[0];
        if (framelen == 0x19) {
            return;
        }
        byte[] matching1 = new byte[]{0x08, 0x02, 0x00, 0x02, 0x04, 0x00, (byte) 0xF3, 0x03};
        byte[] matching2 = new byte[]{0x06, 0x02, (byte) 0xFF, 0x00, 0x04, 0x03};
        byte[] matching = new byte[]{0x06, 0x01, 0x00, 0x00, (byte) 0xf8, 0x03};
        if (getDataChar(buffer, matching)) {
            byte[] bute = new byte[]{0x40, 0x52, 0x46, 0x30, 0x30, 0x30, 0x30, 0x38, 0x08, 0x02, 0x42, 0x02, (byte) 0x93, 0x00, 0x26, 0x03};
            Common.SendSerial(bute);
            return;
        }
        if (getDataChar(buffer, matching1)) {
            byte[] appdata = Common.GetFormat("2305", 1, 1, new String[]{"1"});
            Common.ServicesAllSend(appdata);
            return;
        }
        if (getDataChar(buffer, matching2)) {
            byte[] appdata = Common.GetFormat("2305", 1, 1, new String[]{"2"});
            Common.ServicesAllSend(appdata);
            return;
        }
        if (buffer[0] != 0x0a) {
            byte[] appdata = Common.GetFormat("2308", 1, 1, new String[]{Common.encode(buffer)});
            Common.ServicesAllSend(appdata);
            return;
        }
        int len = buffer[3];
        byte[] strBuffer = new byte[len];
        System.arraycopy(buffer, 4, strBuffer, 0, len);
        byte[] appdata = Common.GetFormat("2306", 1, 1, new String[]{Common.encode(strBuffer)});
        Common.ServicesAllSend(appdata);
    }

    public boolean getDataChar(byte[] stringBuffer, byte[] matching) {
        try {
            boolean result = true;
            if (matching.length > stringBuffer.length)
                return false;
            for (int x = 0; x < matching.length; x++) {
                if (matching[x] != stringBuffer[x]) {
                    result = false;
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //public String str = "";
//    try {
//        str += new String(buffer)+"\r\n" ;
//        if (start < end)
//            return;
//
//        str = "";
//    } catch (Exception e) {
//        str = "";
//    }

}
