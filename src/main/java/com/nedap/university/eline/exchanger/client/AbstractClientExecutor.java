package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class AbstractClientExecutor {
	
	public int getNewServerPort(final byte[] choiceByte, final byte[] dataBytes, final InetAddress serverAddress, 
			final int serverPort, final DatagramSocket socket) {
    	DatagramPacket packet = makeDataPacket(choiceByte, dataBytes, serverAddress, serverPort);
    	sendToServer(packet, socket);
    	DatagramPacket response = receivePacket(socket);
    	return response.getPort();
    }
    
    public DatagramPacket makeDataPacket(final byte[] choiceByte, final byte[] dataBytes, 
    		final InetAddress serverAddress, final int serverPort) {
		byte[] packetBytes = new byte[dataBytes.length + choiceByte.length];
		System.arraycopy(choiceByte, 0, packetBytes, 0, choiceByte.length);
		System.arraycopy(dataBytes, 0, packetBytes, choiceByte.length, dataBytes.length);
		return new DatagramPacket(packetBytes, packetBytes.length, serverAddress, serverPort);
	}
    
    public void sendToServer(DatagramPacket packet, DatagramSocket socket) {
    	try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send the choice to the server.");
		}
    }
    
    public DatagramPacket receivePacket(DatagramSocket socket) {
		DatagramPacket response = null;
    	try {
			response = new DatagramPacket(new byte[1], 1);
			socket.receive(response);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return response;
    }

}
