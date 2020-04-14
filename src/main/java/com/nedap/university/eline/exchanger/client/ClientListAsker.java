package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientListAsker extends AbstractClientExecutor {
	
	private int generalServerPort;
	private InetAddress serverAddress;
	private String listFileLocation;
	
	public ClientListAsker(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
		//TODO doesn't work on Windows!
		listFileLocation = System.getProperty ("user.home") + "/Desktop/listOfFilesOnServer.txt";
	}
	
	public void letClientAskForList() {
		try {
			String choice = CommunicationMessages.LIST;
			byte[] choiceIndicator = choice.getBytes("UTF-8");
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			final int specificServerPort = getNewServerPort(choiceIndicator, new byte[0], serverAddress, generalServerPort, thisCommunicationsSocket);
			
			if (Files.exists(Paths.get(listFileLocation))) {
				Files.delete(Paths.get(listFileLocation));
			}
			
			new FileReceiveManager(thisCommunicationsSocket, serverAddress, specificServerPort, listFileLocation, "listOfFilesOnServer.txt").receiveFile();
			
			printListOfFiles();
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
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
			String[] fileTextSplitByFile = fileText.split(CommunicationMessages.SEPARATION_TWO_FILES);
			System.out.println("The files on the server are (name -- size in bytes):");
			for (String file : fileTextSplitByFile) {
				String[] oneFileInfo = file.split(CommunicationMessages.SEPARATION_NAME_SIZE);
				System.out.printf("%-70s %-70s\n", "Name: " + oneFileInfo[0], "Size (in bytes): " + oneFileInfo[1]);
			}
			
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
