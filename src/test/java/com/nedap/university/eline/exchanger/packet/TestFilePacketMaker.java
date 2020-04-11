package com.nedap.university.eline.exchanger.packet;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

public class TestFilePacketMaker {
	
	@Test
	public void testMakeDataPacket() throws UnknownHostException {
		final int port = 8080;
		final FilePacketMaker maker = new FilePacketMaker(new byte[10000], InetAddress.getLocalHost(), port);
		final Optional<DatagramPacket> packet = maker.makeDataPacket(11, 11);
		
		assertEquals(port, packet.get().getPort());
		assertEquals(InetAddress.getLocalHost(), packet.get().getAddress());
		assertEquals(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE, packet.get().getLength());
	}
	
	@Test
	public void testMakeLastNotFullDataPacket() throws UnknownHostException {
		final int port = 8080;
		final int numberBytes = 10000;
		final FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port);
		final int lastPacketNumber = 10000/FilePacketContents.DATASIZE;
		final Optional<DatagramPacket> packet = maker.makeDataPacket(lastPacketNumber, lastPacketNumber);
		
		assertEquals(port, packet.get().getPort());
		assertEquals(InetAddress.getLocalHost(), packet.get().getAddress());
		assertNotEquals(FilePacketContents.DATASIZE, packet.get().getLength());
	}
	
	@Test
	public void testMakeLastPlusOneDataPacket() throws UnknownHostException {
		final int port = 8080;
		final int numberBytes = 10000;
		final FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port);
		final int lastPacketNumber = 10000/FilePacketContents.DATASIZE + 1;
		final Optional<DatagramPacket> packet = maker.makeDataPacket(lastPacketNumber, lastPacketNumber);
		
		assertEquals(true, packet == null);
	}
	
	@Test
	public void testMakeByteArrayForPacket() throws UnknownHostException {
		final FilePacketMaker maker = new FilePacketMaker(new byte[10000], InetAddress.getLocalHost(), 8080);
		final byte[] allBytes = maker.makeByteArrayForPacket(11, 11, 0);
		
		assertEquals(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE, allBytes.length);
	}
	
	@Test
	public void testMakeHeader() throws UnknownHostException {
		final FilePacketMaker maker = new FilePacketMaker(new byte[10000], InetAddress.getLocalHost(), 8080);
		final byte[] headerBytes = maker.makeHeader(10, 0);
		
		assertEquals(FilePacketContents.HEADERSIZE, headerBytes.length);
	}
	
	@Test
	public void testMakeBody() throws UnknownHostException {
		final FilePacketMaker maker = new FilePacketMaker(new byte[10000], InetAddress.getLocalHost(), 8080);
		final byte[] bodyBytes = maker.makeBody(10);
		
		assertEquals(FilePacketContents.DATASIZE, bodyBytes.length);
	}
	

}
