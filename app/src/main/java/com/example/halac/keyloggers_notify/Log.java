package com.example.halac.keyloggers_notify;

public class Log {
	private String context;
	private String type;
	private String dateAndtime;

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDateAndTime() {
		return dateAndtime;
	}

	public void setDateAndTime(String dateAndTime) {
		this.dateAndtime = dateAndTime;
	}

	
    public String toString() {
    		return context + " " + type + " " + dateAndtime  + "\n";
    }

}
