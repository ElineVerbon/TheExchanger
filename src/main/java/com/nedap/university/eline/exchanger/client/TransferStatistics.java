package com.nedap.university.eline.exchanger.client;

import java.util.concurrent.TimeUnit;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class TransferStatistics {
	
	
	public static void printStatistics(final String clientString, final String serverString) {
		
		if (clientString.length() == 0 && serverString.length() == 0) {
			ClientTUI.showMessage("No data so show yet, please finish a transfer first.");
			return;
		}
		
		ClientTUI.showMessage(String.format("%-40s %-20s %-15s %-20s %-20s\n", 
				"Filename ", "| File size (bytes) ", "| Up/download ", "| Transmission time ", "| Number retransmissions"));
		
		String[] clientFiles = clientString.split(CommunicationStrings.SEPARATION_TWO_FILES);
		for (String fileStatistics : clientFiles) {
			ClientTUI.showMessage(fileStatistics);
		}
		String[] serverFiles = serverString.split(CommunicationStrings.SEPARATION_TWO_FILES);
		for (String fileStatistics : serverFiles) {
			ClientTUI.showMessage(fileStatistics);
		}
	}
	
	public static String getElapsedTime(final long startTime, final long stopTime) {
		long milliSecondsPassed = TimeUnit.MILLISECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS);
		long hours = milliSecondsPassed / (3600*1000);
		long minutes = milliSecondsPassed / (60*1000);
		long seconds = milliSecondsPassed / 1000;
		
		return hours + "h " + minutes + "m " + seconds + "s " + milliSecondsPassed + "ms";
	}
}
