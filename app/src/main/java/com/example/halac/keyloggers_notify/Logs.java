package com.example.halac.keyloggers_notify;


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
