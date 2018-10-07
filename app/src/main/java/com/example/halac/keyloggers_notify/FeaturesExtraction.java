package com.example.halac.keyloggers_notify;

import java.util.ArrayList;
import java.util.List;

public class FeaturesExtraction {
	User session; 
	List<FeatureExtractor> extractors;

	public FeaturesExtraction() {
		this.extractors = new ArrayList<FeatureExtractor>(); 
	}
	
	public void addExtractor(FeatureExtractor fe) {
		extractors.add(fe);
	}
	
	/*
	public void generateFeatures() {
		for(FeatureExtractor extractor: extractors) {
			features.add(extractor.extractFeature());
		}
	}*/
	
	public List<FeatureExtractor> getFreatures(){
		return extractors;
	}
	
	public String toString() {
		return extractors.toString();
	}
}
