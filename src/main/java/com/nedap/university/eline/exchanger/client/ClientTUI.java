package com.nedap.university.eline.exchanger.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;


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
	
	
	public static void printHelpMenu() {
		showMessage("Type one of the following single characters, followed by hitting enter to execute the corresponding action.\n"
				+ " l: request a list of all files present on the Server. This list will be downloaded and the content printed here\n"
				+ " u: upload a file to the server\n d: download a file from the server\n w: withdraw (remove) a file from the server\n"
				+ " r: replace a file on the server with a local file\n p: pause the download of a file\n c: continue the paused download of a file\n"
				+ " s: print the statistics of the transfers so far\n e: exit the program");
	}
	
	/**
	 * Prints the question and asks the user to input a String.
	 * 
	 * @param question, a String representing the question to show to the user
	 * @return a user-defined String
	 */
	public static String getChoice() {
		ClientTUI.showMessage("What do you want to do? (Type h and hit enter for help.)");
		String userInput = "";
		boolean correctInput = true;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		do {
			try {
				userInput = in.readLine();
				if(!CommunicationStrings.possibleChoices().contains(userInput)) {
					correctInput = false;
					showMessage("Only 'u', 'd', 'w', 'r', 'p', 'c', 'e' and 'h' are acceptable as answers. Please try again. (Type h for help.)");
				} else {
					correctInput = true;
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
	
	public static boolean getBoolean(String question) {
		showMessage(question);
		boolean validInput = false;
		boolean userBoolean = false;
		
		while (!validInput) {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String userInput = "";
			try {
				userInput = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (userInput.equalsIgnoreCase("yes") || userInput.equalsIgnoreCase("y")) { 
				userBoolean = true; validInput = true;
			} else if (userInput.equalsIgnoreCase("no") || userInput.equalsIgnoreCase("n")) { 
				userBoolean = false; validInput = true;
			} else { 
				showMessage("Sorry, this is not valid input, please enter yes or no");
			}
		}
		
		return userBoolean;
	}
}
