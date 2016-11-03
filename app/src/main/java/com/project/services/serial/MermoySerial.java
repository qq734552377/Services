package com.project.services.serial;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/6/3.
 */
public class MermoySerial {
    private static Map<String, PadSerialPort> map = new ConcurrentHashMap<String, PadSerialPort>();

    public static void Add(PadSerialPort channel) {

        map.put(channel.Name, channel);
    }

    public static PadSerialPort GetChannel(String name) {

        return map.get(name);
    }

    public static void Remove(String key) {
       map.remove(key);
    }

    public static Set<Map.Entry<String, PadSerialPort>> ToList()
    {
        return map.entrySet();
    }
}
