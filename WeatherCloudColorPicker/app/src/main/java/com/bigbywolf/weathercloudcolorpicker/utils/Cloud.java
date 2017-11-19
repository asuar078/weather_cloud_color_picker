package com.bigbywolf.weathercloudcolorpicker.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bigbywolf on 11/12/17.
 */

public class Cloud implements Parcelable{


    private String device_name = null;
    private String ip = null;
    private int port = 0;

    public enum Mode {
        Auto,
        Manual,
        Clear,
        BlueSky,
        WhiteClouds,
        Overcast,
        Sunset,
        Rain,
        Cloudy
        // disco
    }

    private int mode = 0;
    private int color = 0;

    public Cloud(String ip, int port, String device_name, int mode, int color){
        this.ip = ip;
        this.port = port;
        this.device_name = device_name;
        this.mode = mode;
        this.color = color;
    }

    protected Cloud(Parcel in) {
        ip = in.readString();
        port = in.readInt();
        mode = in.readInt();
        color = in.readInt();
    }

    public static final Creator<Cloud> CREATOR = new Creator<Cloud>() {
        @Override
        public Cloud createFromParcel(Parcel in) {
            return new Cloud(in);
        }

        @Override
        public Cloud[] newArray(int size) {
            return new Cloud[size];
        }
    };

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ip);
        dest.writeInt(port);
        dest.writeInt(mode);
        dest.writeInt(color);
    }



    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

}
