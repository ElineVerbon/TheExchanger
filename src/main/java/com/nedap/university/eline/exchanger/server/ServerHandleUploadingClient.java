package com.nedap.university.eline.exchanger.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ServerHandleUploadingClient {

    public void letUserUploadFile(final DatagramPacket packet) {
    	try {
    		final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	byte[] choiceByte = Arrays.copyOfRange(packet.getData(), 0, 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			
			String absoluteFilePath = System.getProperty ("user.home") + "/Desktop/" + fileName;
			new FileReceiveManager(thisCommunicationsSocket, absoluteFilePath, fileName).receiveFile();
//	    	String absoluteFilePath = "/home/pi/fileLocalTestUpload.pdf";
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
