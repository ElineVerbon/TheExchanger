package com.nedap.university.eline.exchanger.communication;

import java.util.ArrayList;
import java.util.List;

public class CommunicationStrings {
	
	//to communicate what the user wants to do
	public static final String UPLOAD = "u";
	public static final String DOWNLOAD = "d";
	public static final String LIST = "l";
	public static final String WITHDRAW = "w";
	public static final String REPLACE = "r";
	public static final String EXIT = "e";
	public static final String PAUSE = "p";
	public static final String CONTINUE = "c";
	public static final String HELP = "h";
	
	//for pausing and resuming a download
	public static final String NO_SUCH_THREAD = "n";
	public static final String FINISHED = "f";
	public static final String INTERRUPTED = "i";
	public static final String SUCCESS = "s";
	
	public static List<String> possibleChoices() {
		List<String> possibleChoices = new ArrayList<>();
		possibleChoices.add(UPLOAD);
		possibleChoices.add(DOWNLOAD);
		possibleChoices.add(LIST);
		possibleChoices.add(WITHDRAW);
		possibleChoices.add(REPLACE);
		possibleChoices.add(EXIT);
		possibleChoices.add(PAUSE);
		possibleChoices.add(CONTINUE);
		possibleChoices.add(HELP);
		return possibleChoices;
	}
	
	public static List<String> possiblePauseAndResumeOutcomes() {
		List<String> possiblePauseAndResumeOutcomes = new ArrayList<>();
		possiblePauseAndResumeOutcomes.add(NO_SUCH_THREAD);
		possiblePauseAndResumeOutcomes.add(FINISHED);
		possiblePauseAndResumeOutcomes.add(INTERRUPTED);
		possiblePauseAndResumeOutcomes.add(SUCCESS);
		return possiblePauseAndResumeOutcomes;
	}
	
	public static byte[] toBytes(final String communicationString) {
		if (!possibleChoices().contains(communicationString) 
				&& !possiblePauseAndResumeOutcomes().contains(communicationString)) {
			throw new IllegalArgumentException();
		} else {
			return communicationString.getBytes();
		}
	}
	
	public static final String SEPARATION_NAME_SIZE = ";";
	public static final String SEPARATION_TWO_FILES = "/";
	
}
