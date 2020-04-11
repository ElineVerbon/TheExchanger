package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAckPacketMaker {

	final int port = 8080;
	AckPacketMaker maker;
	
	@Test
	public void testMakePacket() throws UnknownHostException {
		maker = new AckPacketMaker(InetAddress.getLocalHost(), port);
		DatagramPacket packet = maker.makePacket(true, true, 10);
		
		assertEquals(port, packet.getPort());
		assertEquals(InetAddress.getLocalHost(), packet.getAddress());
		assertEquals(AckPacketContents.getAckPacketLength(), packet.getLength());
	}
}
