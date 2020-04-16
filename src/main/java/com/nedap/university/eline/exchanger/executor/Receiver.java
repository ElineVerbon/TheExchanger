package com.nedap.university.eline.exchanger.executor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class Receiver {
	
	private DatagramSocket socket;
	
	public Receiver(DatagramSocket socket) {
		this.socket = socket;
	}
	
	public DatagramPacket receivePacket(final int bufferLength) throws SocketTimeoutException {
		return receivePacket(bufferLength, 2 * FilePacketSender.timeOutTime * 1000);
	}
	
	public DatagramPacket receivePacket(final int bufferLength, final int timeout) throws SocketTimeoutException {
		DatagramPacket response = null;
			response = new DatagramPacket(new byte[bufferLength], bufferLength);
			try {
				socket.setSoTimeout(FilePacketSender.timeOutTime * 1000);
				socket.receive(response);
			} catch (SocketTimeoutException e) {
				throw new SocketTimeoutException();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		return response;
	}
}
