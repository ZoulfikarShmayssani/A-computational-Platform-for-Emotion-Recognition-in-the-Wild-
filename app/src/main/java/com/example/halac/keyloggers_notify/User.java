package com.example.halac.keyloggers_notify;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by halac on 10/31/17.
 */

public class User {
    String fname;
    String lname;
    String age;
    String mood;
    String gender;
    String comments;
    String dateAndtime;

    public User(String fname, String lname, String age, String mood, String gender, String comments) {
        this.fname = fname;
        this.lname = lname;
        this.age = age;
        this.gender = gender;
        this.mood = mood;
        this.comments = comments;
        dateAndtime = this.getCurrentTimeStamp();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

}
