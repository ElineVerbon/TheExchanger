package com.nedap.university.eline.exchanger.server;

import java.io.*;
import java.net.*;

import com.nedap.university.eline.exchanger.shared.SlidingWindowReceiver;

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
            server.receiveAndSaveFile();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } 
    }
    
    public void receiveAndSaveFile() {
    	String absoluteFilePath = System.getProperty ("user.home") + "/Desktop/fileLocalTestUpload.pdf";
    	File file;
    	
        try {
        	file = new File(absoluteFilePath);
			if(file.createNewFile()){
			    System.out.println(absoluteFilePath+" File Created in " + file.getAbsolutePath());
			    byte[] bytes = new SlidingWindowReceiver(socket).receiveFile();
			    System.out.println("Number of bytes is " + bytes.length);
		    	try {
		    		OutputStream os = new FileOutputStream(file);
					os.write(bytes);
		    		os.close();
		    		System.out.println("File was saved to " + file.getAbsolutePath());
		    	} catch (IOException e) {
					System.out.println("Saving file to pi failed. Error message: " + e.getMessage());
				}
			    
			    
			} else System.out.println("File "+absoluteFilePath+" already exists");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
    }
}

