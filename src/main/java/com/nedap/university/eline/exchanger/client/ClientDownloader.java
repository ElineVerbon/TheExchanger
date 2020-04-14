package com.nedap.university.eline.exchanger.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientDownloader extends AbstractClientExecutor {
	
	private ClientListAsker listAsker;
	
	public ClientDownloader(int serverPort, InetAddress serverAddress) {
		super(serverPort, serverAddress);
		listAsker = new ClientListAsker(serverPort, serverAddress);
	}
	
	public void letClientDownloadFile() {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.DOWNLOAD);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			String fileName = letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to download. "
					+ "Note: you need to type the entire file name, including extension.", listAsker);
			byte[] fileNameBytes = fileName.getBytes();
			
			final int specificServerPort = letServerKnowWhatTheClientWantsToDoAndGetAServerPort(
					choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			//TODO doesn't work on windows!
			String listFileLocation = System.getProperty ("user.home") + "/Desktop/" + fileName;
			new FileReceiveManager(thisCommunicationsSocket, getServerAddress(), specificServerPort, listFileLocation, fileName).receiveFile();
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		}
	}
}
