package com.nedap.university.eline.exchanger.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;
import com.nedap.university.eline.exchanger.manager.FileSendManager;
import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;

public class ServerHandlerReplacingClient {
	
	private List<FileReceiveManager> managers;
	
	public ServerHandlerReplacingClient() {
		managers = new ArrayList<>();
	}
	
	public void letUserReplaceFile(final DatagramPacket packet) {
		try {
    		final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	
	    	final byte[] allBytes = packet.getData();
	    	final byte[] checksumBytes = Arrays.copyOf(allBytes, ChecksumGenerator.CHECKSUM_LENGTH);
	    	
	    	byte[] choiceByte = Arrays.copyOfRange(allBytes, ChecksumGenerator.CHECKSUM_LENGTH, ChecksumGenerator.CHECKSUM_LENGTH + 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			
			String absoluteFilePath = Server.ACCESSIBLE_FOLDER + fileName;
			Files.deleteIfExists(Paths.get(absoluteFilePath));
			FileReceiveManager manager = new FileReceiveManager(thisCommunicationsSocket, 
					clientAddress, clientPort, Server.ACCESSIBLE_FOLDER, fileName, checksumBytes);
			startAndSaveNewThreadToReceiveFile(manager);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void startAndSaveNewThreadToReceiveFile(final FileReceiveManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
		managers.add(manager);
	}
	
    public void stopAllThreads() {
    	for (FileReceiveManager manager : managers) {
    		manager.stopRunning();
    	}
    }
}
