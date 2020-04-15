package com.nedap.university.eline.exchanger.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class ClientRemover extends AbstractClientExecutor {
	
	private ClientListAsker listAsker;
	
	public ClientRemover(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		listAsker = new ClientListAsker(serverPort, serverAddress);
	}
	
	public void letClientRemoveFile() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.WITHDRAW);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			String fileName = letUserEnterTheNameOfAFileOnTheServer("Please type the name of name of the file you wish to remove and hit enter."
					+ "Note: you need to type the entire file name, including extension.", listAsker);
			byte[] fileNameBytes = fileName.getBytes();
			
			if (fileName.equals("x")) {
				thisCommunicationsSocket.close();
				return;
			}
			
			DatagramPacket packet = makeDataPacket(choiceIndicator, fileNameBytes, getServerAddress(), getGeneralServerPort());
			sendToServer(packet, thisCommunicationsSocket);
	    	DatagramPacket response = receivePacket(thisCommunicationsSocket);
	    	byte[] responseBytes = response.getData();
			
			if ((responseBytes[0] &0xFF) == 1) { 
				ClientTUI.showMessage("The file was successfully removed!");
			} else {
				ClientTUI.showMessage("Sorry, file removal was unsuccessfull.");
			}
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		}
	}
}
