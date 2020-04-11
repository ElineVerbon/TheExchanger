package com.nedap.university.eline.exchanger.executor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver {
	
	private DatagramSocket socket;
	
	public Receiver(DatagramSocket socket) {
		this.socket = socket;
	}
	
	public DatagramPacket receivePacket(final int bufferLength) {
		DatagramPacket response = null;
    	try {
			response = new DatagramPacket(new byte[bufferLength], bufferLength);
			socket.receive(response);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return response;
    }

}
