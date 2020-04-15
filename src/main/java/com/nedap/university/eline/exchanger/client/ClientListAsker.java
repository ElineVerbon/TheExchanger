package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientListAsker extends AbstractClientExecutor {
	
	private DirectoryChooser directoryChooser;
	
	public ClientListAsker(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		directoryChooser = new DirectoryChooser();
	}
	
	public String letClientAskForList() {
		String fileNames = "";
		
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.LIST);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			
			directoryChooser.chooseDirectory("Please type the directory in which you want to save the downloaded file.", 
					"listOfFilesOnServer.txt");
			
			final int specificServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(
					choiceIndicator, new byte[0], thisCommunicationsSocket);
			
			new FileReceiveManager(thisCommunicationsSocket, getServerAddress(), specificServerPort, 
					directoryChooser.getDirectory(), "listOfFilesOnServer.txt").receiveFile();
			
			fileNames = printListOfFiles();
			
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to aks for the list of files on the server failed.");
		}
		
		return fileNames;
	}
	
	public String printListOfFiles() {
		Path path = Paths.get(directoryChooser.getAbsolutePath());
		waitUntilFileExists(path);
		byte[] fileBytes = getFileContentsOnceDownloaded(path);
		
		String fileText = new String(fileBytes);
		String[] fileTextSplitByFile = fileText.split(CommunicationStrings.SEPARATION_TWO_FILES);
		return printList(fileTextSplitByFile);
	}
	
	public void waitUntilFileExists(final Path path) {
		while(!Files.exists(path)) {
			waitABit();
		}
	}
	
	public byte[] getFileContentsOnceDownloaded(final Path path) {
		byte[] fileBytes = null;
		
		try {
			boolean fileFilled = false;
			while(!fileFilled) {
				fileBytes = Files.readAllBytes(path);
				if (fileBytes.length == 0) {
					waitABit();
				} else {
					fileFilled = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		return fileBytes;
	}
	
	public void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Transmitter at sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
		}
	}
	
	public String printList(final String[] fileTextSplitByFile) {
		ClientTUI.showMessage("The files on the server are (name -- size in bytes):");
		String fileNames = "";
		for (String file : fileTextSplitByFile) {
			String[] oneFileInfo = file.split(CommunicationStrings.SEPARATION_NAME_SIZE);
			System.out.printf("%-70s %-70s\n", "Name: " + oneFileInfo[0], "Size (in bytes): " + oneFileInfo[1]);
			fileNames = fileNames + CommunicationStrings.SEPARATION_TWO_FILES + oneFileInfo[0];
		}
		return fileNames;
	}

}
