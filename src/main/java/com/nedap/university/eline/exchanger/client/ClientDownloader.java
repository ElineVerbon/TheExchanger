package com.nedap.university.eline.exchanger.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.DuplicateDownloadException;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientDownloader {
	
	private ClientListAsker listAsker;
	private ChoiceCommunicator communicator;
	
	private DirectoryChooser directoryChooser;
	private FileChooser fileChooser;
	private Map<String, Thread> startedThreads;
	private String fileName;
	
	public ClientDownloader(final ChoiceCommunicator communicator, final ClientListAsker asker) {
		this.listAsker = asker;
		this.communicator = communicator;
		directoryChooser = new DirectoryChooser();
		fileChooser = new FileChooser();
		startedThreads = new HashMap<>();
	}
	
	public void letClientDownloadFile() throws UserQuitToMainMenuException, SocketTimeoutException {
		fileName = fileChooser.letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to download. "
				+ "Note: you need to type the entire file name, including extension.", listAsker);
	
		downloadFile();
		boolean moreDownloads = ClientTUI.getBoolean("Do you want to download another file?");
		
		while(moreDownloads) {
			fileName = fileChooser.letUserEnterAnotherName();
			downloadFile();
		}
	}
	
	public void downloadFile() throws UserQuitToMainMenuException, SocketTimeoutException  {
		
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.DOWNLOAD);
			
			//TODO add max waiting time for the receive method in getNewServerPort()!
						
			byte[] fileNameBytes = fileName.getBytes();
			
			checkForDuplicateDownload();
			
			directoryChooser.chooseDirectory("Please type the directory in which you want to save the downloaded file.", fileName);
			
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			final int specificServerPort = response.getPort();
			
			FileReceiveManager manager = new FileReceiveManager(thisCommunicationsSocket, communicator.getServerAddress(), specificServerPort, directoryChooser.getDirectory(), fileName);
			startAndSaveNewThreadToReceiveFile(fileName, manager);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		} catch (DuplicateDownloadException e) {
			ClientTUI.showMessage("A thread is currently running to download this file. The download will not be started to prevent duplicate threads."
					+ " You can download it again once the currently running download is finished.");
		}
	}
	
	private void startAndSaveNewThreadToReceiveFile(final String fileName, final FileReceiveManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
		startedThreads.put(fileName, thread);
	}
	
	private void checkForDuplicateDownload() throws DuplicateDownloadException {
		if (startedThreads.containsKey(fileName)) {
			if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
					|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				throw new DuplicateDownloadException();
			}
		}
	}
}
