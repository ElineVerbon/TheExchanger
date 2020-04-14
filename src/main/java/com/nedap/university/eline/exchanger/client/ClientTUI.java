package com.nedap.university.eline.exchanger.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;


/** 
 * This class is responsible for getting user input from the console.
 */

public class ClientTUI {
	
	/** 
	 * Constructor. 
	 */
	public ClientTUI() {
	}
	
	/**
	 * Writes the given message to standard output.
	 * 
	 * @param msg the message to write to the standard output.
	 */
	public static void showMessage(String message) {
		System.out.println(message);
	}
	
	/**
	 * Prints the question and asks the user to input a String.
	 * 
	 * @param question, a String representing the question to show to the user
	 * @return a user-defined String
	 */
	public static String getChoice() {
		String userInput = "";
		boolean correctInput = true;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		do {
			try {
				userInput = in.readLine();
				if(!CommunicationMessages.possibleChoices().contains(userInput)) {
					correctInput = false;
					showMessage("Only 'd', 'u' and 'e' are acceptable as answers. Please try again.");
				}
			} catch (IOException e) {
				System.out.println("Could not read user input. Error message: " + e.getMessage());
			}
		} while (!correctInput);
		
		return userInput;
	}
	
	public static String getString() {
		String userInput = "";
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			userInput = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return userInput;
	}
}
