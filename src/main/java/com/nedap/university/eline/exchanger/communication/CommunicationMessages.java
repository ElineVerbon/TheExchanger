package com.nedap.university.eline.exchanger.communication;

import java.util.ArrayList;
import java.util.List;

public class CommunicationMessages {
	
	public static final String UPLOAD = "u";
	public static final String DOWNLOAD = "d";
	public static final String LIST = "l";
	public static final String WITHDRAW = "w";
	public static final String REPLACE = "r";
	public static final String EXIT = "e";
	
	public static List<String> possibleChoices() {
		List<String> possibleChoices = new ArrayList<>();
		possibleChoices.add(UPLOAD);
		possibleChoices.add(DOWNLOAD);
		possibleChoices.add(LIST);
		possibleChoices.add(WITHDRAW);
		possibleChoices.add(REPLACE);
		possibleChoices.add(EXIT);
		return possibleChoices;
	}
	
	
	

}
