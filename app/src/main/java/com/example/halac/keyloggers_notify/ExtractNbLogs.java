package com.example.halac.keyloggers_notify;

import java.util.List;
/*
Extract the number of logs
 */
public class ExtractNbLogs implements FeatureExtractor {

	private double count = 0.0;
	
	public ExtractNbLogs(List<Log> logs) {
		count = logs.size();
	}
	
	@Override
	public double extractFeature() {
		return count;
	}
	
	public String toString() {
		return count + "";
	}
	

}
