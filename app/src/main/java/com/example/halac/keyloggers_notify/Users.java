package com.example.halac.keyloggers_notify;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Users {
    String fname;
    String lname;
    String age;
    String mood;
    String gender;
    String comments;
    String dateAndtime;

    public Users(String fname, String lname, String age, String gender) {
        this.fname = fname;
        this.lname = lname;
        this.age = age;
        this.gender = gender;
        dateAndtime = this.getCurrentTimeStamp();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

}
