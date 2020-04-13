package com.nedap.university.eline.exchanger.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.Test;

import com.nedap.university.eline.exchanger.window.SendingWindow;

public class TestAckReceiver {
	
	@Test
	public void testProcessAck() throws UnknownHostException, SocketException {
		SendingWindow sendingWindow = new SendingWindow();
		SentFilePacketTracker tracker = new SentFilePacketTracker();
		FilePacketSender sender = new FilePacketSender(new DatagramSocket(), tracker, sendingWindow);
		AckReceiver receiver = new AckReceiver(new DatagramSocket(), tracker, sendingWindow, sender);
		
		//add first 'sent' packet to tracker
		final DatagramPacket packet = new DatagramPacket(new byte[] { (byte) 0b00000000 }, 0);
		tracker.addPacket(0, packet);
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(0));
		assertEquals(false, receiver.isDuplicateAck(0));
		
		//process ack just added
		receiver.processAck(0);
		assertEquals(false, tracker.getSentNotAckedPackets().containsKey(0));
		assertEquals(0, sendingWindow.getDuplicateACKs());
		assertEquals(true, receiver.isDuplicateAck(0));
		assertEquals(false, receiver.isDuplicateAck(1));
		
		//give the same ack again
		receiver.processAck(0);
		assertEquals(1, sendingWindow.getDuplicateACKs());
		
		receiver.processAck(0);
		receiver.processAck(0);
		assertEquals(0, sendingWindow.getDuplicateACKs());
	}
	
	

}
