package com.nedap.university.eline.exchanger.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	
	private ClientTUI clientTUI;
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private final int serverPort = 8080;
	private final String hostname = "nu-pi-stefan";
	private boolean clientIsDone = false;
	
	public Client() {
	    try {
	    	clientTUI = new ClientTUI();
	    	serverAddress = InetAddress.getByName(hostname);
			socket = new DatagramSocket();
			clientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
		} catch (SocketException e) {
			System.out.println("Socket could not be opened. Error message: " + e.getMessage());
		} catch (UnknownHostException e) {
			System.out.println("InetAddress could not be resolved. Error message: " + e.getMessage());
		}
	}
	
    public static void main(String[] args) {
    	Client client = new Client();
    	client.start();
    }
    
    public void start() {
    	
    	String usersChoice = clientTUI.getChoice("Do you want to download, upload or exit? (d, u or e)");
    	
    	while(!clientIsDone) {
            if(usersChoice.equals("u")) {
            	new ClientUploader(clientTUI, serverPort, serverAddress, socket).uploadFile();
            } else if(usersChoice.equals("e")) {
            	clientIsDone = true;
            }
            
            usersChoice = clientTUI.getChoice("Do you want to download or upload something else or do you want to exit? (d, u or e)");
    	}
    }
}


