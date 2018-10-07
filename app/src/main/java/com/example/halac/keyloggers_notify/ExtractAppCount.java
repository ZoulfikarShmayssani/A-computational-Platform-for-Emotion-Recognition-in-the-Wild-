package com.example.halac.keyloggers_notify;

import java.util.List;

public class ExtractAppCount implements FeatureExtractor {

	private double count = 0.0;
	
	public ExtractAppCount(List<Log> logs, String s) {
		for(Log log: logs) {
			if(log.getContext().startsWith(s)) {
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