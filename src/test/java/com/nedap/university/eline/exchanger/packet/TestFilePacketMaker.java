package com.nedap.university.eline.exchanger.packet;

import org.junit.Test;

import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.SentFilePacketTracker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker.CanSend;
import com.nedap.university.eline.exchanger.window.SendingWindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		
		for (int i = 0; i < SendingWindow.SENDING_WINDOW_SIZE; i++) {
			assertEquals(CanSend.YES, maker.sendNextPacketIfPossible());
		}
		assertEquals(CanSend.NOT_IN_WINDOW, maker.sendNextPacketIfPossible());
	}
	
	@Test
	public void testMakeLastDataPacket() throws UnknownHostException, SocketException {
		final int port = 8080;
		final int numberBytes = FilePacketContents.DATASIZE * SendingWindow.SENDING_WINDOW_SIZE - 1;
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), new SentFilePacketTracker(), new SendingWindow());
		FilePacketMaker maker = new FilePacketMaker(new byte[numberBytes], InetAddress.getLocalHost(), port, new SendingWindow(), sender);
		
		for (int i = 0; i < SendingWindow.SENDING_WINDOW_SIZE; i++) {
			maker.sendNextPacketIfPossible();
		}
		assertEquals(CanSend.NO_MORE_PACKETS, maker.sendNextPacketIfPossible());
	}
}
