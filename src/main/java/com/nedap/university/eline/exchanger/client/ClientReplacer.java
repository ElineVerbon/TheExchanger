package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;
import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ClientReplacer {
	
	private ClientListAsker listAsker;
	private ChoiceCommunicator communicator;
	private FileChooser fileChooser;
	
	public ClientReplacer(final ChoiceCommunicator communicator, final ClientListAsker asker) {
		this.listAsker = asker;
		this.communicator = communicator;
		this.fileChooser = new FileChooser();
	}
	
	public void letClientReplaceFile() throws UserQuitToMainMenuException {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.REPLACE);
		
			String fileName = fileChooser.letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to replace. "
					+ "Note: you need to type the entire file name, including extension.", listAsker);
			File toBeUploadedFile = fileChooser.getUserSelectedLocalFile("Please type the absolute filepath of the file you want to upload."
					+ " Note: the file will be saved under the name of the file you chose to replace.");
			
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			byte[] fileNameBytes = fileName.getBytes();
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			final int specificServerPort = response.getPort();
			
			final String fileNamesForInResultString = toBeUploadedFile.getName() + " to replace " + fileName;
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			
			FileSendManager manager = new FileSendManager(fileBytes, communicator.getServerAddress(), specificServerPort, thisCommunicationsSocket, fileNamesForInResultString);
			startNewThreadToSendFile(manager);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startNewThreadToSendFile(final FileSendManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
	}
}