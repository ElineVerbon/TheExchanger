package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientListAsker extends AbstractClientExecutor {
	
	private String listFileLocation;
	
	public ClientListAsker(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		//TODO doesn't work on Windows!
		listFileLocation = System.getProperty ("user.home") + "/Desktop/listOfFilesOnServer.txt";
	}
	
	public void letClientAskForList() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.LIST);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			final int specificServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(
					choiceIndicator, new byte[0], thisCommunicationsSocket);
			
			if (Files.exists(Paths.get(listFileLocation))) {
				Files.delete(Paths.get(listFileLocation));
			}
			
			new FileReceiveManager(thisCommunicationsSocket, getServerAddress(), specificServerPort, listFileLocation, "listOfFilesOnServer.txt").receiveFile();
			
			printListOfFiles();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printListOfFiles() {
		//move file to bin if is exists
		
		try {
			Path path = Paths.get(listFileLocation);
			byte[] fileBytes = null;
			
			boolean fileFilled = false;
			while(!Files.exists(path)) {
				waitABit();
			}
			
			while(!fileFilled) {
				fileBytes = Files.readAllBytes(path);
				if (fileBytes.length == 0) {
					waitABit();
				} else {
					fileFilled = true;
				}
			}
			
			String fileText = new String(fileBytes);
			String[] fileTextSplitByFile = fileText.split(CommunicationStrings.SEPARATION_TWO_FILES);
			System.out.println("The files on the server are (name -- size in bytes):");
			for (String file : fileTextSplitByFile) {
				String[] oneFileInfo = file.split(CommunicationStrings.SEPARATION_NAME_SIZE);
				System.out.printf("%-70s %-70s\n", "Name: " + oneFileInfo[0], "Size (in bytes): " + oneFileInfo[1]);
			}
			ClientTUI.showMessage("You can also find this list here: " + listFileLocation);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Transmitter at sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
		}
	}

}
