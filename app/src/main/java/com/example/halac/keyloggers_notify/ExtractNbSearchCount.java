package com.example.halac.keyloggers_notify;

import java.util.List;
/*
Extract the search number
 */
public class ExtractNbSearchCount implements FeatureExtractor {

	private double count = 0.0;
	
	public ExtractNbSearchCount(List<Log> logs) {
		for(Log log: logs) {
			if(log.getContext().startsWith("[Search")) {
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
