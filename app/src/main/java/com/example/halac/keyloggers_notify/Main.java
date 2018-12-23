package com.example.halac.keyloggers_notify;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

//extracting the events that we want

public class Main {
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException, ParseException {
		//JSON to object
		ObjectMapper objectMapper = new ObjectMapper();
		Sessions users = objectMapper.readValue(new File("keyloggers.json"), Sessions.class);
		List<User> listUsers = users.getSessions();
		
		PrintWriter writer = new PrintWriter("file.txt", "UTF-8");
		PrintWriter writer2 = new PrintWriter("file2.txt", "UTF-8");
		PrintWriter writer3 = new PrintWriter("size.txt", "UTF-8");
		writer3.print(listUsers.size());

		for(User user: listUsers) {
			OutputList l = new OutputList();
			Double[] userInfo = new Double[3];
			l.getInfo(user).toArray(userInfo);
			for(int i = 0; i < 3; i++) 
				writer.print(userInfo[i] + " ");
			writer.println();
			
			List<Log> listLogs = user.getLogs();
			FeaturesExtraction f = new FeaturesExtraction();
			
			//get click count (1)
			f.addExtractor(new ExtractNbEvent(listLogs, "CLICKED"));
			
			//get long click count (2)
			f.addExtractor(new ExtractNbEvent(listLogs, "LONG CLICKED"));
			
			//get scrolls count (3)
			f.addExtractor(new ExtractNbEvent(listLogs, "SCROLLED"));
			
			//get text count (4)
			f.addExtractor(new ExtractNbEvent(listLogs, "TEXT"));
			
			//get focused count (5)
			f.addExtractor(new ExtractNbEvent(listLogs, "FOCUSED"));
			
			//get number of change window (6)
			f.addExtractor(new ExtractNbEvent(listLogs, "CHANGE"));
			
			//get logs count (7)
			f.addExtractor(new ExtractNbLogs(listLogs));
			
			//get time in Facebook (8)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Facebook).*"));
			
			//get time in Whatsapp (9)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Whatsapp).*"));
			
			//get time in Instagram (10)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Instagram).*"));
			
			//get time in Camera (11)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Camera).*"));
			
			//get time in Gallery (12)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Gallery).*"));
			
			//get time in Email (13)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(Email|Gmail|Outlook).*"));
			
			//get time in Youtube (14)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(YouTube).*"));
			
			//time spent in games (15)
			f.addExtractor(new ExtractTimeSpent(listLogs, ".*(PrincessSalon|Candy Crush Saga|Six Guns).*"));
			
			//get number of  Camera (16)
			f.addExtractor(new ExtractAppCount(listLogs, "[Camera]"));
			
			//get number of  Phone (17)
			f.addExtractor(new ExtractAppCount(listLogs, "[Phone]"));
			
			// count calls (18)
			f.addExtractor(new ExtractAppCount(listLogs, "[Dialing"));
			
			//add social media ??
			
			//get word count (19)
			f.addExtractor(new ExtractNumWords(listLogs));
			
			//get search count (20)
			f.addExtractor(new ExtractNbSearchCount(listLogs));
			
			//number of Youtube videos (21)
			f.addExtractor(new ExtractnNbYoutubeVideo(listLogs));
			
			// print all the features for all users
			Object[] inputs = new Object[21];
			f.getFreatures().toArray(inputs);
			for(int i = 0; i < 21; i++) 
				writer2.print(inputs[i] + " ");
			writer2.println();
		}
		
		writer3.close();
		writer2.close();
		writer.close();
	}

}
