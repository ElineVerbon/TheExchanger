package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class ClientTerminator {
	
	private ChoiceCommunicator communicator;
	
	public ClientTerminator(final ChoiceCommunicator communicator) {
		this.communicator = communicator;
	}
	
	public void endProgram() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.EXIT);
		
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			communicator.communicateChoiceToServer(choiceIndicator, new byte[0], thisCommunicationsSocket);
			
			ClientTUI.showMessage("Goodbye!");
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
