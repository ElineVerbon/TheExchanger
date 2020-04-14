package com.nedap.university.eline.exchanger.client;

import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientListAsker extends AbstractClientExecutor {
	
	private int generalServerPort;
	private InetAddress serverAddress;
	
	public ClientListAsker(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
	public void letClientAskForList() {
		try {
			String choice = CommunicationMessages.LIST;
			byte[] choiceIndicator = choice.getBytes("UTF-8");
					
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			final int specificServerPort = getNewServerPort(choiceIndicator, new byte[0], serverAddress, generalServerPort, thisCommunicationsSocket);
			
			String absoluteFilePath = System.getProperty ("user.home") + "/Desktop/listOfFilesOnServer.txt";
			new FileReceiveManager(thisCommunicationsSocket, serverAddress, specificServerPort, absoluteFilePath, "listOfFilesOnServer.txt").receiveFile();
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
