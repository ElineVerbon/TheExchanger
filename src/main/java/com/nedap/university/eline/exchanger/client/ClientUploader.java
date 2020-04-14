package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ClientUploader extends AbstractClientExecutor implements ClientUploaderInterface {

	private int generalServerPort;
	private InetAddress serverAddress;
	
	public ClientUploader(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
    public void letClientUploadFile() {
    	
		try {
			String choice = "u";
			byte[] choiceIndicator = choice.getBytes("UTF-8");
			
			File toBeUploadedFile = getUserSelectedFile();
			String fileName = toBeUploadedFile.getName();
			byte[] fileNameBytes = fileName.getBytes("UTF-8");
					
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			final int thisCommunicationsServerPort = getNewServerPort(choiceIndicator, fileNameBytes, serverAddress, 
					generalServerPort, thisCommunicationsSocket);
			
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			new FileSendManager(fileBytes, serverAddress, thisCommunicationsServerPort, thisCommunicationsSocket, fileName).sendFile();
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public File getUserSelectedFile() {
    	ClientTUI.showMessage("Please type in the absolute filepath of the file you want to upload.");
    	String absoluteFilePath = ClientTUI.getString();
    	File file = new File(absoluteFilePath);

    	while (!file.exists()) {
    		ClientTUI.showMessage("");
    		ClientTUI.showMessage("The file could not be found. Please try again.");
    		absoluteFilePath = ClientTUI.getString();
        	file = new File(absoluteFilePath);
    	}
    	
    	return file;
    }	
       
}
