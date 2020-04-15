package com.nedap.university.eline.exchanger.server;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
    private DatagramSocket generalSocket;
    private ServerHandlerUploadingClient serverHandlerUploadingClient;
    private ServerHandlerListAskingClient serverHandlerListAskingClient;
    private ServerHandlerDownloadingClient serverHandlerDownloadingClient;
    private ServerHandlerRemovingClient serverHandlerRemovingClient;
    private ServerHandlerReplacingClient serverHandlerReplacingClient;
    
    boolean done = false;
    
//    public static final String ACCESSIBLE_FOLDER = "/home/pi/accessibleFolder/";
    public static final String ACCESSIBLE_FOLDER = System.getProperty ("user.home") + "/Desktop/";
 
    public Server(int port) {
        try {
			generalSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        serverHandlerUploadingClient = new ServerHandlerUploadingClient();
        serverHandlerListAskingClient = new ServerHandlerListAskingClient();
        serverHandlerDownloadingClient = new ServerHandlerDownloadingClient();
        serverHandlerRemovingClient = new ServerHandlerRemovingClient();
        serverHandlerReplacingClient = new ServerHandlerReplacingClient();
    }
 
    public static void main(String[] args) {
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
    	DatagramPacket choicePacket = null;
    	try {
    		byte[] buffer = new byte[1500];
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
    	
    	if (choice.equals(CommunicationStrings.UPLOAD)) {
    		serverHandlerUploadingClient.letUserUploadFile(choicePacket);
    	} else if (choice.equals(CommunicationStrings.LIST)) {
        	serverHandlerListAskingClient.letUserAskForList(choicePacket);
    	} else if (choice.equals(CommunicationStrings.DOWNLOAD)) {
    		serverHandlerDownloadingClient.letUserDownloadFile(choicePacket);
    	} else if (choice.equals(CommunicationStrings.WITHDRAW)) {
    		serverHandlerRemovingClient.letUserRemoveFile(choicePacket);
    	} else if (choice.equals(CommunicationStrings.REPLACE)) {
    		serverHandlerReplacingClient.letUserReplaceFile(choicePacket);
    	} else if (choice.equals(CommunicationStrings.PAUSE)) {
    		serverHandlerDownloadingClient.pauseAThread(choicePacket);
    	} else if (choice.equals(CommunicationStrings.CONTINUE)) {
    		serverHandlerDownloadingClient.resumeAThread(choicePacket);
    	} else if (choice.equals(CommunicationStrings.EXIT)) {
    		sendResponseWithOnlyChoiceByte(choicePacket);
    		serverHandlerDownloadingClient.stopAllThreads();
    		serverHandlerUploadingClient.stopAllThreads();
    		serverHandlerReplacingClient.stopAllThreads();
    	}
    }
    
    public void sendResponseWithOnlyChoiceByte(final DatagramPacket packet) {
    	try {
    		final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	byte[] choiceByte = Arrays.copyOfRange(packet.getData(), 0, 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
	    	thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
	    	thisCommunicationsSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

