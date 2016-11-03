package com.project.services.Model;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;

public class PictureModel {

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> BufferPicture = new ArrayList();

    private long outTime;

    private int total;

    private Channel channel;

    private String path;

}
