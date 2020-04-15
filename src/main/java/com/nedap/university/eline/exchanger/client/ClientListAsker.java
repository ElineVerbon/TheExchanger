package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientListAsker {
	
	private DirectoryChooser directoryChooser;
	private ChoiceCommunicator communicator;
	
	public ClientListAsker(final ChoiceCommunicator communicator) {
		this.communicator = communicator;
		directoryChooser = new DirectoryChooser();
	}
	
	public String letClientAskForList() throws UserQuitToMainMenuException, SocketTimeoutException {
		String fileNames = "";
		DatagramSocket thisCommunicationsSocket = null;
		
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.LIST);
			thisCommunicationsSocket = new DatagramSocket();
			
			directoryChooser.chooseDirectory("Please type the directory in which you want to save the downloaded file.", 
					"listOfFilesOnServer.txt");
			
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, new byte[0], thisCommunicationsSocket);
			final int specificServerPort = response.getPort();
			
			new FileReceiveManager(thisCommunicationsSocket, communicator.getServerAddress(), specificServerPort, 
					directoryChooser.getDirectory(), "listOfFilesOnServer.txt").run();
			
			fileNames = printListOfFiles();
			
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to aks for the list of files on the server failed.");
		} catch (UserQuitToMainMenuException e) {
			thisCommunicationsSocket.close();
			throw new UserQuitToMainMenuException();
		}
		
		return fileNames;
	}
	
	private String printListOfFiles() {
		Path path = Paths.get(directoryChooser.getAbsolutePath());
		waitUntilFileExists(path);
		byte[] fileBytes = getFileContentsOnceDownloaded(path);
		
		String fileText = new String(fileBytes);
		if (fileText.equals("No files present on the server.")) {
			ClientTUI.showMessage("No files present on the server.");
			return "";
		}
		ClientTUI.showMessage(fileText);
		String[] fileTextSplitByFile = fileText.split(CommunicationStrings.SEPARATION_TWO_FILES);
		return printList(fileTextSplitByFile);
	}
	
	private void waitUntilFileExists(final Path path) {
		while(!Files.exists(path)) {
			waitABit();
		}
	}
	
	private byte[] getFileContentsOnceDownloaded(final Path path) {
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
	
	private void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Transmitter at sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
		}
	}
	
	private String printList(final String[] fileTextSplitByFile) {
		ClientTUI.showMessage("");
		ClientTUI.showMessage("The files on the server:");
		System.out.printf("%-60s %-3s %-15s\n", "Name ", " | ", "Size (in bytes) ");
		String fileNames = "";
		for (String file : fileTextSplitByFile) {
			String[] oneFileInfo = file.split(CommunicationStrings.SEPARATION_NAME_SIZE);
			System.out.printf("%-60s %-3s %-15s\n", oneFileInfo[0], " | ", oneFileInfo[1]);
			fileNames = fileNames + CommunicationStrings.SEPARATION_TWO_FILES + oneFileInfo[0];
		}
		ClientTUI.showMessage("");
		return fileNames;
	}

}
