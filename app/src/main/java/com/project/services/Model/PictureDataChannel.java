package com.project.services.Model;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/8/18.
 */
public class PictureDataChannel {

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;

    public String getChannelId() {
        return ChannelId;
    }

    public void setChannelId(String channelId) {
        ChannelId = channelId;
    }

    private String ChannelId;
}
