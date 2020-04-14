package com.nedap.university.eline.exchanger.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Arrays;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ServerHandlerDownloadingClient {

	public void letUserDownloadFile(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	byte[] choiceByte = Arrays.copyOfRange(packet.getData(), 0, 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			File file = new File("/home/pi/" + fileName);
			final byte[] fileBytes = Files.readAllBytes(file.toPath());
			
			new FileSendManager(fileBytes, clientAddress, clientPort, thisCommunicationsSocket, fileName).sendFile();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] getListOfFiles() {
		byte[] fileList = null;
		try {
			String allFiles = "";
			File directory = new File("/home/pi");
//			File directory = new File(System.getProperty ("user.home") + "/Desktop");
			for (File file : directory.listFiles()) {
				allFiles = allFiles + file.getName() + CommunicationStrings.SEPARATION_NAME_SIZE + file.length() + CommunicationStrings.SEPARATION_TWO_FILES;
			}
			fileList = allFiles.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		}
		return fileList;
	}
}
