package com.example.halac.keyloggers_notify;
import java.util.ArrayList;
import java.util.List;

public class User {
	private List<Log> Logs;
	private String age;
	private String comments;
	private String dateAndtime;
	private String fname;
	private String lname;
	private String gender;
	private String mood;
	
	
	public User(){
		Logs = new ArrayList<Log>();
	}

	public List<Log> getLogs() {
		
		//remove last elements
		for(int i =0; i< Logs.size(); i++) {
			if(Logs.get(i).getContext().equals("[KeyLoggers-Notify]")&&Logs.get(i).getType().equals("CLICKED")) Logs = Logs.subList(0, i);
		}
		
		//reduce to 3minutes
		long sT = Long.parseLong(Logs.get(0).getDateAndTime());
		long l = sT + 180000;
		long eT = 0;
		int index = 0;
		for(int i = 0; i< Logs.size(); i++) {
			
			if (eT <= l) {
				eT = Long.parseLong(Logs.get(i).getDateAndTime());
				index = i;
			}
		}
		Logs = Logs.subList(0, index+1);
		return Logs;
		
	}
	
	public void setLogs(List<Log> Logs) {
		this.Logs = Logs;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getDateAndtime() {
		return dateAndtime;
	}

	public void setDateAndtime(String dateAndtime) {
		this.dateAndtime = dateAndtime;
	}

	public String getLname() {
		return lname;
	}

	public void setLname(String lname) {
		this.lname = lname;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}
	
	public int getCount() {
		return Logs.size();
	}
	
	  public String toString() {
			String s = fname + " " + lname + " " + gender + " " + age + " " + mood + " " + comments + "  " + dateAndtime + " \n";
			for(Log log: Logs) s+= log;
			return s;
	  }
	 

}