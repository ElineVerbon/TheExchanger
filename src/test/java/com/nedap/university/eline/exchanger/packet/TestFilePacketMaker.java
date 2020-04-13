package com.nedap.university.eline.exchanger.packet;

import org.junit.Test;

import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.SentFilePacketTracker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker.CanSend;
import com.nedap.university.eline.exchanger.window.SendingWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TestFilePacketMaker {
	
	@Test
	public void testCanSendNextPacket() throws SocketException, UnknownHostException {
		final int port = 8080;
		final int numberBytes = FilePacketContents.DATASIZE * SendingWindow.SENDING_WINDOW_SIZE + 1;
		SendingWindow sendingWindow = new SendingWindow();
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), sendingWindow);
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, sendingWindow, sender);
		
		assertEquals(CanSend.YES, maker.canSendNextPacket());
		for (int i = 0; i < SendingWindow.SENDING_WINDOW_SIZE; i++) {
			assertEquals(CanSend.YES, maker.canSendNextPacket());
			maker.makeAndSendPacket();
		}
		assertEquals(CanSend.NOT_IN_WINDOW, maker.canSendNextPacket());
	}
	
	@Test
	public void testMakeDataPacket() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = 10000;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final DatagramPacket packet = maker.makeDataPacket();
		
		assertEquals(port, packet.getPort());
		assertEquals(InetAddress.getLocalHost(), packet.getAddress());
		assertEquals(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE, packet.getLength());
	}
	
	@Test
	public void testMakeDataPacketOutOfWindow() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = FilePacketContents.DATASIZE * SendingWindow.SENDING_WINDOW_SIZE + 1;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		
		for (int i = 0; i < SendingWindow.SENDING_WINDOW_SIZE; i++) {
			maker.makeAndSendPacket();
		}
		
		assertThrows(IllegalArgumentException.class, () -> maker.makeAndSendPacket());
	}
	
	@Test
	public void testMakeLastNotFullDataPacket() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = 10000;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final int lastPacketNumber = 10000/FilePacketContents.DATASIZE;
		final DatagramPacket packet = maker.makeDataPacket();
		
		assertEquals(port, packet.getPort());
		assertEquals(InetAddress.getLocalHost(), packet.getAddress());
		assertNotEquals(FilePacketContents.DATASIZE, packet.getLength());
	}
	
	@Test
	public void testMakeLastPlusOneDataPacket() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = FilePacketContents.DATASIZE * SendingWindow.SENDING_WINDOW_SIZE - 1;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final int lastPacketNumber = 10000/FilePacketContents.DATASIZE + 1;
		final DatagramPacket packet = maker.makeDataPacket();
		
		assertEquals(CanSend.YES, maker.canSendNextPacket());
		for (int i = 0; i < (SendingWindow.SENDING_WINDOW_SIZE - 1); i++) {
			assertEquals(CanSend.YES, maker.canSendNextPacket());
			maker.makeAndSendPacket();
		}
		assertEquals(CanSend.NO_MORE_PACKETS, maker.canSendNextPacket());
	}
	
	@Test
	public void testMakeByteArrayForPacket() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = 10000;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final byte[] allBytes = maker.makeByteArrayForPacket(11, 11, 0);
		
		assertEquals(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE, allBytes.length);
	}
	
	@Test
	public void testMakeHeader() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = 10000;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final byte[] headerBytes = maker.makeHeader(10, 0);
		
		assertEquals(FilePacketContents.HEADERSIZE, headerBytes.length);
	}
	
	@Test
	public void testMakeBody() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = 10000;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		final byte[] bodyBytes = maker.makeBody(10);
		
		assertEquals(FilePacketContents.DATASIZE, bodyBytes.length);
	}
	

}
