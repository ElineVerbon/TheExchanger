package com.nedap.university.eline.exchanger.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientDownloader extends AbstractClientExecutor {
	
	private ClientListAsker listAsker;
	private DirectoryChooser directoryChooser;
	private Map<String, Thread> startedThreads;
	
	public ClientDownloader(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		listAsker = new ClientListAsker(serverPort, serverAddress);
		directoryChooser = new DirectoryChooser();
		startedThreads = new HashMap<>();
	}
	
	public void letClientDownloadFile() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.DOWNLOAD);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			String fileName = letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to download. "
					+ "Note: you need to type the entire file name, including extension.", listAsker);
			
			if (fileName.equals("x")) {
				thisCommunicationsSocket.close();
				return;
			}
			
			byte[] fileNameBytes = fileName.getBytes();
			
			if (startedThreads.containsKey(fileName)) {
				if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
						|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
					ClientTUI.showMessage("A thread is currently running to download this file. The download will not be started to prevent duplicate threads."
							+ " You can download it again once the currently running download is finished.");
					thisCommunicationsSocket.close();
					return;
				}
			}
			
			directoryChooser.chooseDirectory("Please type the directory in which you want to save the downloaded file.", fileName);
			
			final int specificServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(
					choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			
			FileReceiveManager manager = new FileReceiveManager(thisCommunicationsSocket, getServerAddress(), specificServerPort, directoryChooser.getDirectory(), fileName);
			startAndSaveNewThreadToReceiveFile(fileName, manager);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		}
	}
	
	public void startAndSaveNewThreadToReceiveFile(final String fileName, final FileReceiveManager manager) {
		Thread thread = new Thread(() -> manager.receiveFile());
		thread.start();
		startedThreads.put(fileName, thread);
	}
}
