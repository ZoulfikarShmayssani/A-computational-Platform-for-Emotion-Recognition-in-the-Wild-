package com.example.halac.keyloggers_notify;

import java.util.ArrayList;
import java.util.List;

public class OutputList {
	enum Mood {
		NEUTRAL, HAPPY, SAD,  TIRED, ANGRY, STRESSED
	}
	enum Gender {
		F, M
	}
	
	
	public List<Double> getInfo(User u) {
		String age = u.getAge();
		String gender = u.getGender();
		
		List<Double> user = new ArrayList<Double>();
		user.add((double) Integer.parseInt(age));
	    
	    for (Gender c : Gender.values()) {
	        if (c.name().equals(gender.toUpperCase())) {
	        		user.add((double) c.ordinal());
	        }
	    }
		return user;
	}
}