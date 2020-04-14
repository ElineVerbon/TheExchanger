package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ClientUploader extends AbstractClientExecutor {
	
	public ClientUploader(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
	}
	
    public void letClientUploadFile() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.UPLOAD);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			
			File toBeUploadedFile = getUserSelectedLocalFile("Please type in the absolute filepath of the file you want to upload.");
			String fileName = toBeUploadedFile.getName();
			byte[] fileNameBytes = fileName.getBytes("UTF-8");
					
			final int thisCommunicationsServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(
					choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			new FileSendManager(fileBytes, getServerAddress(), thisCommunicationsServerPort, thisCommunicationsSocket, fileName).sendFile();
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to upload a file failed.");
		} catch (IOException e) {
			ClientTUI.showMessage("The file you are trying to upload could not be read.");
		} 
	}	  
}
