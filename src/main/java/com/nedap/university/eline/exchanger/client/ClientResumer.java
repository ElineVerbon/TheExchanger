package com.nedap.university.eline.exchanger.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class ClientResumer extends AbstractClientExecutor {

	public ClientResumer(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
	}
	
	public void letClientResumeDownload() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.CONTINUE);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()
			
			ClientTUI.showMessage("Please type the name of name of the file with a pause download that you want to resume"
					+ " and hit enter. Note: you need to type the entire file name, including extension.");
			String fileName = ClientTUI.getString();
			byte[] fileNameBytes = fileName.getBytes();
			
			DatagramPacket packet = makeDataPacket(choiceIndicator, fileNameBytes, getServerAddress(), getGeneralServerPort());
			sendToServer(packet, thisCommunicationsSocket);
	    	DatagramPacket response = receivePacket(thisCommunicationsSocket); //add maximum waiting time
	    	byte[] responseBytes = response.getData();
			
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
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		}
	}

}
