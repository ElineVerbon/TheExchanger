package com.nedap.university.eline.exchanger.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nedap.university.eline.exchanger.manager.FileReceiveManager;
import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;

public class ServerHandlerUploadingClient {
	
	private List<FileReceiveManager> managers;
	
	public ServerHandlerUploadingClient() {
		managers = new ArrayList<>();
	}

    public void letUserUploadFile(final DatagramPacket packet) {
    	try {
    		final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	final byte[] checksumBytes = getChecksum(packet);
	    	final String fileName = getFileName(packet);
	    	
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
	    	makeAndSendConfirmationToClient(packet.getData(), clientAddress, clientPort, thisCommunicationsSocket);
			
			FileReceiveManager manager = new FileReceiveManager(thisCommunicationsSocket, clientAddress, 
					clientPort, Server.ACCESSIBLE_FOLDER, fileName, checksumBytes);
			startAndSaveNewThreadToReceiveFile(fileName, manager);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void startAndSaveNewThreadToReceiveFile(final String fileName, final FileReceiveManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
		managers.add(manager);
	}
    
    public void stopAllThreads() {
    	for (FileReceiveManager manager : managers) {
    		manager.stopRunning();
    	}
    }
    
    private byte[] getChecksum(final DatagramPacket packet) {
    	final byte[] allBytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
    	final byte[] checksumBytes = Arrays.copyOfRange(allBytes, 1, 1 + ChecksumGenerator.CHECKSUM_LENGTH);
    	
    	return checksumBytes;
    }
    
    private String getFileName(final DatagramPacket packet) {
    	byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1 + ChecksumGenerator.CHECKSUM_LENGTH, packet.getLength());
		final String fileName = new String(fileNameBytes);
		return fileName;
    }
    
    private void makeAndSendConfirmationToClient(final byte[] allBytes, InetAddress clientAddress, final int clientPort, 
    		final DatagramSocket socket) {
    	byte[] choiceByte = Arrays.copyOfRange(allBytes, ChecksumGenerator.CHECKSUM_LENGTH, ChecksumGenerator.CHECKSUM_LENGTH + 1);
    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
		try {
			socket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
