package com.example.administrator.test;

/**
 * Created by My on 2017/12/7.
 */

public class SmsBean {
    public String address;
    public String body;
    public String date;
    public String type;

    @Override
    public String toString() {
        return "SmsBean{" +
                "address='" + address + '\'' +
                ", body='" + body + '\'' +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
