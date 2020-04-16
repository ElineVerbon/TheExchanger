package com.nedap.university.eline.exchanger.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileSendManager;
import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;

public class ServerHandlerListAskingClient {

	public void letUserAskForList(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	byte[] choiceByte = Arrays.copyOfRange(packet.getData(), 0, 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
	    	
	    	byte[] listBytes = getListOfFiles();
	    	
			thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
			
			new FileSendManager(listBytes, clientAddress, clientPort, thisCommunicationsSocket, "listOfFilesOnServer.txt").run();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private byte[] getListOfFiles() {
		String allFiles = "";
		File directory = new File(Server.ACCESSIBLE_FOLDER);
		for (File file : directory.listFiles()) {
			allFiles = allFiles + file.getName() + CommunicationStrings.SEPARATION_NAME_SIZE + file.length() + CommunicationStrings.SEPARATION_TWO_FILES;
		}
		if (allFiles.length() == 0) {
			String result = "No files present on the server.";
			return result.getBytes();
		} else {
			return allFiles.getBytes();
		}
	}
}
