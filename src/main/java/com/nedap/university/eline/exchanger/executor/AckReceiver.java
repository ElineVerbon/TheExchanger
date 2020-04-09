package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AckReceiver extends AbstractReceiver {
	
	private boolean lastAck = false;
	
	public AckReceiver(final DatagramSocket socket) {
		super(socket);
	}
	
	public int getAck() {
		DatagramPacket response = receivePacket(3);
    	lastAck = ((response.getData()[0] &0xFF) == 1) ? true : false;
		return response.getData()[2] &0xFF;
	}
	
	public boolean isDone() {
		return lastAck;
	}

}
