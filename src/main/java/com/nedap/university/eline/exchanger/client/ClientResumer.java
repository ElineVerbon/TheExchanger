package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class ClientResumer {

private ChoiceCommunicator communicator;
	
	public ClientResumer(final ChoiceCommunicator communicator) {
		this.communicator = communicator;
	}
	
	public void letClientResumeDownload() throws SocketTimeoutException {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.CONTINUE);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()
			
			//send to server that I want to continue downloads
	    	DatagramPacket response = communicator.communicateChoiceToServerAndExpectLongerResponse(choiceIndicator, 
	    			new byte[0], thisCommunicationsSocket);
			ClientTUI.showMessage("These files are currently in a paused download:");
	    	
			String pausedFileNames = new String(response.getData());
		
			ClientTUI.showMessage("Please type the name of name of the file with a paused download that you want to resume"
					+ " and hit enter. Note: you need to type the entire file name, including extension.");
			String fileName = ClientTUI.getString();
			byte[] fileNameBytes = fileName.getBytes();
			
			//send to server that I want to continue downloads
	    	response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes,thisCommunicationsSocket);
	    	//receive message with all currently paused downloads
	    	byte[] responseBytes = response.getData();
	    	//let server know which one I want to continue
			
	    	updateUser(responseBytes);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		}
	}
	
	public void updateUser(final byte[] responseBytes) {
		String serversResponse = new String(responseBytes);
    	if (serversResponse.equals(CommunicationStrings.NO_SUCH_THREAD)) { 
			ClientTUI.showMessage("No thread known for this filename.");
		} else if (serversResponse.equals(CommunicationStrings.FINISHED)) { 
			ClientTUI.showMessage("The download was already finished.");
		} else if (serversResponse.equals(CommunicationStrings.INTERRUPTED)) {
			ClientTUI.showMessage("The download was not paused.");
		} if (serversResponse.equals(CommunicationStrings.SUCCESS)) {
			ClientTUI.showMessage("The download was successfully resumed.");
		}
	}

}
