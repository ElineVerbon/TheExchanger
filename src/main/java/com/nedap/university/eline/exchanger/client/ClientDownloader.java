package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientDownloader extends AbstractClientExecutor {
	
	private int generalServerPort;
	private InetAddress serverAddress;
	private String listFileLocation;
	
	private ClientListAsker listAsker;
	
	public ClientDownloader(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
		listAsker = new ClientListAsker(serverPort, serverAddress);
		
		//TODO doesn't work on Windows!
		listFileLocation = System.getProperty ("user.home") + "/Desktop";
	}
	
	public void letClientDownloadFile() {
		try {
			String choice = CommunicationMessages.DOWNLOAD;
			byte[] choiceIndicator = choice.getBytes("UTF-8");
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			
			String fileName = getUserSelectedFile();
			byte[] fileNameBytes = fileName.getBytes("UTF-8");
			
			final int specificServerPort = getNewServerPort(choiceIndicator, fileNameBytes, serverAddress, generalServerPort, thisCommunicationsSocket);
			
			new FileReceiveManager(thisCommunicationsSocket, serverAddress, specificServerPort, listFileLocation + "/" + fileName, fileName).receiveFile();
			
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUserSelectedFile() {
		ClientTUI.showMessage("Please be patient, retrieving all files that you can upload.");
		listAsker.letClientAskForList();
    	ClientTUI.showMessage("Please type the name of one of the available files to download. Note: you need to type the entire file name, including extension.");
    	String fileName = ClientTUI.getString();
    	
    	//TODO, would like to test here whether the file exists on the pi (ie is in the list).
    	//TODO, would like to check whether it will overwrite another file ont he desktop.
    	
    	return fileName;
    }
}
