package com.example.halac.keyloggers_notify;
import java.util.*;

public class Sessions {
	private List<User> Sessions;

	public List<User> getSessions() {
		return Sessions;
	}

	public void setSessions(List<User> Sessions) {
		this.Sessions = Sessions;
	}
	
	public String toString() {
		String user  = "";
		for(User u: Sessions) user += u + "\n";
		return user;
	}
}
