package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

import com.nedap.university.eline.exchanger.shared.SlidingWindowTransmitter;

public class ClientUploader implements Runnable {

	private ClientTUI clientTUI;
	private int serverPort;
	private InetAddress serverAddress;
	private DatagramSocket socket;
	
	public ClientUploader(ClientTUI clientTUI, int serverPort, InetAddress serverAddress, DatagramSocket socket) {
		this.clientTUI = clientTUI;
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		clientTUI.showMessage("You chose to upload a file.");
    	
		getFileFromResources().ifPresentOrElse(file -> startTransmitter(file), () -> clientTUI.showMessage("File could not be read."));
	}
	
	public void startTransmitter(File file) {
		//TODO start a new thread here?
		try {
			new SlidingWindowTransmitter(Files.readAllBytes(file.toPath()), serverAddress, serverPort, socket).uploadFile();
		} catch (IOException e) {
			clientTUI.showMessage("File could not be converted to byte. Error message: " + e.getMessage());
		}
	}
    
    // get files from resources folder
    private Optional<File> getFileFromResources() {
    	boolean knownFilename = false;
    	File fileToUpload = null;
    	
    	while(!knownFilename) {
    		String filename = clientTUI.getFileName("Please type the name of the file you wish to upload: ");
	        ClassLoader classLoader = getClass().getClassLoader();
	
	        URL resource = classLoader.getResource("com/nedap/university/eline/exchanger/client/" + filename);
	        if (resource == null) {
	            clientTUI.showMessage("This file is not present on the Client, please try again.");
	            //TODO let user break out of loop
	        } else {
	            fileToUpload = new File(resource.getFile());
	            knownFilename = true;
	        }
    	}
    	return Optional.ofNullable(fileToUpload);
    }
    
    

}
