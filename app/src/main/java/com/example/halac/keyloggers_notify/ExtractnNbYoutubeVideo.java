package com.example.halac.keyloggers_notify;

import java.util.List;

public class ExtractnNbYoutubeVideo implements FeatureExtractor {

	private double count = 0.0;
	
	public ExtractnNbYoutubeVideo(List<Log> logs) {
		for(Log log: logs) {
			if((log.getType().equals("FOCUSED")||log.getType().equals("CLICKED")) && (log.getContext().startsWith("[0") || log.getContext().startsWith("[1") || log.getContext().startsWith("[2") ||
					log.getContext().startsWith("[3") || log.getContext().startsWith("[4") || log.getContext().startsWith("[5") || log.getContext().startsWith("[6") ||
					log.getContext().startsWith("[7") || log.getContext().startsWith("[8") || log.getContext().startsWith("[9"))) {
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
