package com.nedap.university.eline.exchanger.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class TestAckPacketContents {
	
	@Test
	public void testFalsePacket() throws UnknownHostException {
		byte[] bytes = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, 
				(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 };
		FilePacketMaker wrongMaker = new FilePacketMaker(bytes, InetAddress.getLocalHost(), 8080);
		DatagramPacket packet = wrongMaker.makeDataPacket(0, 0).get();
		
		assertThrows(IllegalArgumentException.class, () -> new AckPacketContents(packet));
	}

	@Test
	public void testGetBooleanFromByte() throws UnknownHostException {
		AckPacketMaker maker = new AckPacketMaker(InetAddress.getLocalHost(), 8080);
		DatagramPacket packet = maker.makePacket(true, true, 10);
		AckPacketContents contents = new AckPacketContents(packet);
		
		assertEquals(true, contents.getBooleanFromByte(1));
		assertEquals(false, contents.getBooleanFromByte(0));
		assertThrows(IllegalArgumentException.class, () -> contents.getBooleanFromByte(2));
	}
	
	@Test
	public void testMakePacketAndGetContentInfo() throws UnknownHostException {
		AckPacketMaker maker = new AckPacketMaker(InetAddress.getLocalHost(), 8080);
		final boolean recAllPackets = true;
		final boolean duplicateAck = true;
		final int LFR = 10;
		DatagramPacket packet = maker.makePacket(recAllPackets, duplicateAck, LFR);
		
		AckPacketContents contents = new AckPacketContents(packet);
		
		assertEquals(recAllPackets, contents.isAckOfLastPacket());
		assertEquals(duplicateAck, contents.isDAck());
		assertEquals(LFR, contents.getSeqNum());
	}
}
