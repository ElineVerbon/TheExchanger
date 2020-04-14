package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ClientReplacer extends AbstractClientExecutor {
	
	private ClientListAsker listAsker;
	
	public ClientReplacer(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		listAsker = new ClientListAsker(serverPort, serverAddress);
	}
	
	public void letClientReplaceFile() {
		try {
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.REPLACE);
		
			String fileName = letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to replace. "
					+ "Note: you need to type the entire file name, including extension.", listAsker);
			byte[] fileNameBytes = fileName.getBytes();
			final int specificServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			
			File toBeUploadedFile = getUserSelectedLocalFile("Please type in the absolute filepath of the file you want to upload."
					+ "Note: the file will be saved under the name of the file you chose to replace.");
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			new FileSendManager(fileBytes, getServerAddress(), specificServerPort, thisCommunicationsSocket, fileName).sendFile();
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}