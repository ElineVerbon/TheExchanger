package com.nedap.university.eline.exchanger.communication;

import java.util.ArrayList;
import java.util.List;

public class CommunicationStrings {
	
	public static final String UPLOAD = "u";
	public static final String DOWNLOAD = "d";
	public static final String LIST = "l";
	public static final String WITHDRAW = "w";
	public static final String REPLACE = "r";
	public static final String EXIT = "e";
	public static final String HELP = "h";
	
	public static List<String> possibleChoices() {
		List<String> possibleChoices = new ArrayList<>();
		possibleChoices.add(UPLOAD);
		possibleChoices.add(DOWNLOAD);
		possibleChoices.add(LIST);
		possibleChoices.add(WITHDRAW);
		possibleChoices.add(REPLACE);
		possibleChoices.add(EXIT);
		possibleChoices.add(HELP);
		return possibleChoices;
	}
	
	public static byte[] toBytes(final String communicationString) {
		if (!possibleChoices().contains(communicationString)) {
			throw new IllegalArgumentException();
		} else {
			return communicationString.getBytes();
		}
	}
	
	public static final String SEPARATION_NAME_SIZE = ";";
	public static final String SEPARATION_TWO_FILES = "/";

}
