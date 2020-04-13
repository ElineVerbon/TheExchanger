package com.nedap.university.eline.exchanger.server;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
    private DatagramSocket generalSocket;
    private ServerHandleUploadingClient serverHandlerUploadingClient;
    
    boolean done = false;
 
    public Server(int port) {
        try {
			generalSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        serverHandlerUploadingClient = new ServerHandleUploadingClient();
    }
 
    public static void main(String[] args) {
        System.out.println("Starting server");
    	
    	int port = 8080;
    	
    	Server server = new Server(port);
    	server.start();
    }
    
    public void start() {
    	while(!done) {
    		DatagramPacket choicePacket = getChoice();
    		processChoice(choicePacket);
    	}
    	
    }
    
    public DatagramPacket getChoice() {
    	System.out.println("waiting for a choice");
    	DatagramPacket choicePacket = null;
    	try {
    		byte[] buffer = new byte[1500];
    		System.out.println(buffer.length);
    		choicePacket = new DatagramPacket(buffer, buffer.length);
			generalSocket.receive(choicePacket);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return choicePacket;
    }
    
    public void processChoice(final DatagramPacket choicePacket) {
    	byte[] choiceBytes = choicePacket.getData();
    	byte[] choiceByte = Arrays.copyOfRange(choiceBytes, 0, 1);
    	final String choice = new String(choiceByte);
    	
    	if (choice.equals("u")) {
    		//TODO send back a message with a chosen port.
    		serverHandlerUploadingClient.letUserUploadFile(choicePacket);
    	}
    	
    	if (choice.equals("e")) {
    		//TODO send back a message with a chosen port.
    		done = true;
    	}
    	
    }
}

