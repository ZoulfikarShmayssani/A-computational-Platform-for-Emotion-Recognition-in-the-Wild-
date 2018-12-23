package com.example.halac.keyloggers_notify;

import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
Extracting the time spent in using Apps of different types
 */
public class ExtractTimeSpent implements FeatureExtractor {

	private double count = 0.0;
	private String startTime = "";
	private long sT = 0;
	private long eT = 0;
	private String endTime = ""; 
	
	public ExtractTimeSpent(List<Log> logs, String app) throws ParseException {
		Pattern p = Pattern.compile(app);
		int counter = 0;
		int c2 = 0;
		for(Log log: logs) {
			Matcher m = p.matcher(log.getContext());
			if(m.matches()&& log.getType().equals("CHANGE")) counter++;
		}
		
		for(int i = 0; i <= counter; i++) {
			
			for(Log log: logs) {
				Matcher m = p.matcher(log.getContext());
				if(c2 == 0) {
					if(m.matches() && log.getType().equals("CHANGE")) {
						startTime = log.getDateAndTime();
						sT = Long.parseLong(startTime);
						break;
					}
				} else if (c2 >= 0) {
					if(m.matches() && log.getType().equals("CHANGE") && Long.parseLong(log.getDateAndTime()) >= eT) {
						startTime = log.getDateAndTime();
						sT = Long.parseLong(startTime);
						break;
					}
				}
				c2++;
			}
			
			for(Log log: logs) {
				if(Long.parseLong(log.getDateAndTime()) >= sT && log.getType().equals("CHANGE") && (log.getContext().equals("[Home screen]")||log.getContext().equals("[Recent apps]"))||log.getContext().equals("[Home]"))  {
					endTime = log.getDateAndTime();
					eT = Long.parseLong(endTime);
					break;
				} else {
					eT = Long.parseLong(logs.get(logs.size()-1).getDateAndTime());
					break;
				}
			}
			if (sT == 0) {
				count = 0;
			} else {
				long time = eT - sT;
				count +=  time;

			}
			counter--;
		}
	}
		
	
	@Override
	public double extractFeature() {
		return count;
	}
	
	public String toString() {
		return count + "";
	}

}