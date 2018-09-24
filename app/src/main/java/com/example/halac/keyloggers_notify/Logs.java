package com.example.halac.keyloggers_notify;

/**
 * Created by halac on 10/30/17.
 */

public class Logs {
    String context;
    String type;
    String dateAndTime;

    public Logs(String type, String context , String dateAndTime) {
        this.context = context;
        this.type = type;
        this.dateAndTime = dateAndTime;
    }
}
