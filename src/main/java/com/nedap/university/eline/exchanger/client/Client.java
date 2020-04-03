package com.nedap.university.eline.exchanger.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
public class Client {
	private ClientTUI clientTUI;
	
	DatagramSocket socket;
	InetAddress serverAddress;
	final int serverPort = 8080;
	final String hostname = "nu-pi-stefan.home";
	
	public Client() {
		clientTUI = new ClientTUI();
	}
	
    public static void main(String[] args) {
    	Client client = new Client();
    	
    	client.start();
    }
    
    public void start() {
    	
        try {
            serverAddress = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            
            clientTUI.showMessage("Connection established with \"" + hostname + "\".");
            List<String> acceptableAnswers = new ArrayList<String>(Arrays.asList("d", "u", "e"));
            String usersChoice = clientTUI.getString("Do you want to download, upload or exit? (d, u or e)", acceptableAnswers);
            
            if(usersChoice.equals("u")) {
            	uploadFile();
            }
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } 
    }
    
    public void setUpUDPConnection() throws IOException {
    	DatagramPacket request = new DatagramPacket(new byte[1], 1, serverAddress, serverPort);
        socket.send(request);

       //TODO actually don't think this  is necessary, this info will be send every time a packet is sent
    }
    
    public void uploadFile() {
    	clientTUI.showMessage("You chose to upload a file.");
    	File fileToUpload = getFileFromResources();
    	
    }
    
    // get files from resources folder
    private File getFileFromResources() {
    	boolean knownFilename = false;
    	File fileToUpload = null;
    	
    	while(!knownFilename) {
    		String filename = clientTUI.getFileName("Please type the name of the file you wish to upload:");
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
    	return fileToUpload;
    }
}


