package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramSocket;

public class AckSender extends AbstractSender {
	
	public AckSender(final DatagramSocket socket) {
		super(socket);
	}
	
}
