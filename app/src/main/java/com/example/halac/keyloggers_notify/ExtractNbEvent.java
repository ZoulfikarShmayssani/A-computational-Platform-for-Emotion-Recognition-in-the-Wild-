package com.example.halac.keyloggers_notify;

import java.util.List;

public class ExtractNbEvent implements FeatureExtractor {

	private double count = 0.0;
	
	public ExtractNbEvent(List<Log> logs, String s) {
		for(Log log: logs) {
			if(log.getType().equals(s)) {
				count++;
			}
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