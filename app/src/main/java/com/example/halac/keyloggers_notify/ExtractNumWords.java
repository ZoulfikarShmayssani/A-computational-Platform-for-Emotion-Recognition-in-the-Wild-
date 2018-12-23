package com.example.halac.keyloggers_notify;

import java.util.List;
/*
Extract the number of words
 */

public class ExtractNumWords implements FeatureExtractor {

	private double count = 0;
	
	public ExtractNumWords(List<Log> logs) {
		for(Log log: logs) {
			if(log.getType().equals("TEXT")) {
				String [] s = log.getContext().split(" ");
				count += s.length;
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
