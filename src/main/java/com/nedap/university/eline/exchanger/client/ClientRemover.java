package com.nedap.university.eline.exchanger.client;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;

public class ClientRemover extends AbstractClientExecutor {

	private int generalServerPort;
	private InetAddress serverAddress;
	
	private ClientListAsker listAsker;
	
	public ClientRemover(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
		listAsker = new ClientListAsker(serverPort, serverAddress);
	}
	
	public void letClientRemoveFile() {
		try {
			String choice = CommunicationMessages.WITHDRAW;
			byte[] choiceIndicator = choice.getBytes("UTF-8");
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			String fileName = getUserSelectedFile();
			byte[] fileNameBytes = fileName.getBytes("UTF-8");
			
			DatagramPacket packet = makeDataPacket(choiceIndicator, fileNameBytes, serverAddress, generalServerPort);
			sendToServer(packet, thisCommunicationsSocket);
	    	DatagramPacket response = receivePacket(thisCommunicationsSocket);
	    	byte[] responseBytes = response.getData();
			
			
			if ((responseBytes[0] &0xFF) == 1) { 
				ClientTUI.showMessage("The file was successfully removed!");
			}
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUserSelectedFile() {
		ClientTUI.showMessage("Please be patient, retrieving all files present on the server.");
		listAsker.letClientAskForList();
    	ClientTUI.showMessage("Please type the name of one of the available files to remove it. Note: you need to type the entire file name, including extension.");
    	String fileName = ClientTUI.getString();
    	
    	//TODO, would like to test here whether the file exists on the pi (ie is in the list).
    	//TODO, would like to check whether it will overwrite another file ont he desktop.
    	
    	return fileName;
    }
}
