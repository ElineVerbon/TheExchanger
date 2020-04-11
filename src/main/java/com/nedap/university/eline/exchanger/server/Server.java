package com.nedap.university.eline.exchanger.server;

import java.io.*;
import java.net.*;

import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
    private DatagramSocket socket;
 
    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
 
    public static void main(String[] args) {
        int port = 8080;
 
        try {
            Server server = new Server(port);
//            server.getChoice();
            server.receiveAndSaveFile();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } 
    }
    
    public DatagramPacket getChoice() {
    	DatagramPacket response = null;
    	try {
			response = new DatagramPacket(new byte[1], 1);
			socket.receive(response);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return response;
    	
    }
    
    public void receiveAndSaveFile() {
    	//String absoluteFilePath = System.getProperty ("user.home") + "/Desktop/fileLocalTestUpload.pdf";
    	String absoluteFilePath = "/home/pi/fileLocalTestUpload.pdf";
    	File file;
    	
        try {
        	file = new File(absoluteFilePath);
			if(file.createNewFile()){
			    System.out.println(absoluteFilePath+" File Created in " + file.getAbsolutePath());
			} else {
				System.out.println("File already exists, overwriting it!");
			}
		    byte[] bytes = new FileReceiveManager(socket).receiveFile();
	    	try {
	    		OutputStream os = new FileOutputStream(file);
				os.write(bytes);
	    		os.close();
	    		System.out.println("File was saved to " + file.getAbsolutePath());
	    	} catch (IOException e) {
				System.out.println("Saving file to pi failed. Error message: " + e.getMessage());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}

