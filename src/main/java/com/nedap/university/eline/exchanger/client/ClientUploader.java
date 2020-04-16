package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.DuplicateUploadException;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;
import com.nedap.university.eline.exchanger.manager.FileSendManager;
import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;

public class ClientUploader {
	
	private ChoiceCommunicator communicator;
	private Map<String, Thread> startedThreads;
	private String fileName;
	private FileChooser fileChooser;
	
	public ClientUploader(final ChoiceCommunicator communicator) {
		this.communicator = communicator;
		startedThreads = new HashMap<>();
		this.fileChooser = new FileChooser();
	}
	
    public void letClientUploadFile() throws UserQuitToMainMenuException {
    	try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.UPLOAD);
			
			File toBeUploadedFile = fileChooser.getUserSelectedLocalFile("Please type in the absolute filepath of the file you want to upload.");
			fileName = toBeUploadedFile.getName();
			
			final byte[] checksum = ChecksumGenerator.createChecksumFromFile(toBeUploadedFile);
			
			byte[] fileNameBytes = fileName.getBytes();
			
			checkForDuplicateUpload();
			
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();		
			DatagramPacket response = communicator.communicateChoiceToServerWithChecksum(choiceIndicator, fileNameBytes, checksum, thisCommunicationsSocket);
			final int specificServerPort = response.getPort();
			
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			
			FileSendManager manager = new FileSendManager(fileBytes, communicator.getServerAddress(), specificServerPort, thisCommunicationsSocket, fileName);
			startAndSaveNewThreadToSendFile(fileName, manager);
    	} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to upload a file failed.");
		} catch (IOException e) {
			ClientTUI.showMessage("The file you are trying to upload could not be read.");
		} catch (DuplicateUploadException e) {
			ClientTUI.showMessage("A thread is currently running to upload this file. The upload will not be started."
					+ " You can upload it again once the currently running download is finished.");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
    
	private void startAndSaveNewThreadToSendFile(final String fileName, final FileSendManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
		startedThreads.put(fileName, thread);
	}
	
	private void checkForDuplicateUpload() throws DuplicateUploadException {
		if (startedThreads.containsKey(fileName)) {
			if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
					|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				throw new DuplicateUploadException();
			}
		}
	}
}
