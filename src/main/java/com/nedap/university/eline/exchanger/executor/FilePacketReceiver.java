package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramSocket;

public class FilePacketReceiver extends AbstractReceiver {
	
	public FilePacketReceiver(final DatagramSocket socket) {
		super(socket);
	}

}
