package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class AbstractCommunicator {
	
	public DatagramPacket receivePacket(final DatagramSocket socket, final int bufferLength) {
		DatagramPacket response = null;
    	try {
			response = new DatagramPacket(new byte[bufferLength], bufferLength);
			socket.receive(response);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return response;
    }
	
	public DatagramPacket makeDataPacket(final byte[] headerBytes, final byte[] dataBytes, final InetAddress destAddress, final int destPort) {
		byte[] packetBytes = new byte[dataBytes.length + headerBytes.length];
		System.arraycopy(headerBytes, 0, packetBytes, 0, headerBytes.length);
		System.arraycopy(dataBytes, 0, packetBytes, headerBytes.length, dataBytes.length);
		
		return new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
	}

}
