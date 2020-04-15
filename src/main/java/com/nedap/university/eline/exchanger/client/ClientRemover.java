package com.nedap.university.eline.exchanger.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;

public class ClientRemover {
	
	private ClientListAsker listAsker;
	private ChoiceCommunicator communicator;
	private FileChooser fileChooser;
	private String fileName;
	
	public ClientRemover(final ChoiceCommunicator communicator, final ClientListAsker asker) {
		this.listAsker = asker;
		this.communicator = communicator;
		fileChooser = new FileChooser();
	}
	
	public void letClientRemoveFile() throws UserQuitToMainMenuException, SocketTimeoutException {
		fileName = fileChooser.letUserEnterTheNameOfAFileOnTheServer("Please type the name of name of the file you wish to remove and hit enter."
				+ "Note: you need to type the entire file name, including extension.", listAsker);
	
		removeFile();
		boolean moreRemovals = ClientTUI.getBoolean("Do you want to remove more files?");
		
		while(moreRemovals) {
			fileName = fileChooser.letUserEnterAnotherName();
			removeFile();
			moreRemovals = ClientTUI.getBoolean("Do you want to remove more files?");
		}
	}
	
	private void removeFile() throws SocketTimeoutException {
		
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.WITHDRAW);
			
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			byte[] fileNameBytes = fileName.getBytes();
			
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes,thisCommunicationsSocket);
	    	byte[] responseBytes = response.getData();
			
			if ((responseBytes[0] &0xFF) == 1) { 
				ClientTUI.showMessage("The file was successfully removed!");
			} else {
				ClientTUI.showMessage("Sorry, file removal was unsuccessful.");
			}
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		} 
	}
}
