package com.nedap.university.eline.exchanger.executor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class AbstractSender {
	
	private DatagramSocket socket;
	
	public AbstractSender(DatagramSocket socket) {
		this.socket = socket;
	}
	
	public void sendPacket(final DatagramPacket packet) {
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
