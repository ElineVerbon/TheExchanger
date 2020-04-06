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
	final String hostname = "nu-pi-stefan";
	
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
            	ClientUploader uploader = new ClientUploader(clientTUI, serverPort, serverAddress, socket);
            	//TODO, maybe I should start the Thread in the uploader itself once a file has been chosen
            	new Thread(uploader).start();
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
}


