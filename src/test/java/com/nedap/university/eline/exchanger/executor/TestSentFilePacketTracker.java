package com.nedap.university.eline.exchanger.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.DatagramPacket;
import java.util.Map;

import org.junit.Test;

import com.nedap.university.eline.exchanger.window.AbstractWindow;
import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class TestSentFilePacketTracker {

	@Test
	public void testAddAndRetrievePacket() {
		SentFilePacketTracker tracker = new SentFilePacketTracker();
		DatagramPacket packet0 = new DatagramPacket(new byte[] { (byte) 0b00000000 }, 0);
		DatagramPacket packet1 = new DatagramPacket(new byte[] { (byte) 0b00100000 }, 1);
		tracker.addPacket(0, packet0);
		tracker.addPacket(1, packet1);
		assertEquals(packet0, tracker.getPreviouslySentPacket(0));
		assertEquals(packet1, tracker.getPreviouslySentPacket(1));
		assertNotEquals(packet0, tracker.getPreviouslySentPacket(1));
		
		assertThrows(IllegalArgumentException.class, () -> tracker.addPacket(-1, packet0));
		assertThrows(IllegalArgumentException.class, () -> tracker.getPreviouslySentPacket(2));
	}
	
	@Test 
	public void testRemovePacketAndHasSentPacketBeenAcked() {
		SentFilePacketTracker tracker = new SentFilePacketTracker();
		DatagramPacket packet0 = new DatagramPacket(new byte[] { (byte) 0b00000000 }, 0);
		DatagramPacket packet1 = new DatagramPacket(new byte[] { (byte) 0b00100000 }, 1);
		tracker.addPacket(0, packet0);
		tracker.addPacket(1, packet1);
		tracker.removePacket(0);
		assertEquals(false, tracker.hasSentPacketBeenAcked(1));
		assertEquals(true, tracker.hasSentPacketBeenAcked(0));
		
		assertThrows(IllegalArgumentException.class, () -> tracker.removePacket(-1));
		assertThrows(IllegalArgumentException.class, () -> tracker.removePacket(2));
	}
	
	@Test
	public void testGetSentNotAckedPackets() {
		SentFilePacketTracker tracker = new SentFilePacketTracker();
		DatagramPacket packet0 = new DatagramPacket(new byte[] { (byte) 0b00000000 }, 0);
		DatagramPacket packet1 = new DatagramPacket(new byte[] { (byte) 0b00100000 }, 0);
		DatagramPacket packet2 = new DatagramPacket(new byte[] { (byte) 0b00100000 }, 0);
		DatagramPacket packet3 = new DatagramPacket(new byte[] { (byte) 0b00100000 }, 0);
		tracker.addPacket(0, packet0);
		tracker.addPacket(1, packet1);
		tracker.addPacket(2, packet2);
		tracker.addPacket(3, packet3);
		
		Map<Integer, DatagramPacket> receivedPackets = tracker.getSentNotAckedPackets();
		assertEquals(packet0, receivedPackets.get(0));
		assertEquals(packet1, receivedPackets.get(1));
		assertEquals(packet2, receivedPackets.get(2));
		assertEquals(packet3, receivedPackets.get(3));
		assertNotEquals(packet2, receivedPackets.get(3));
	}
	
	@Test 
	public void updateSentPacketsListAndRemovePacketIfPresent() {
		SentFilePacketTracker tracker = new SentFilePacketTracker();
		DatagramPacket packet0 = new DatagramPacket(new byte[] { (byte) 0b00000000 }, 0);
		for (int i = AbstractWindow.SEQUENCE_NUMBER_SPACE - ReceivingWindow.RECEIVING_WINDOW_SIZE;
				i < AbstractWindow.SEQUENCE_NUMBER_SPACE; i++) {
			tracker.addPacket(i, packet0);
		}
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(AbstractWindow.SEQUENCE_NUMBER_SPACE - 10));
		tracker.updateSentPacketsList(AbstractWindow.SEQUENCE_NUMBER_SPACE - 10,
				AbstractWindow.SEQUENCE_NUMBER_SPACE - ReceivingWindow.RECEIVING_WINDOW_SIZE , AbstractWindow.SEQUENCE_NUMBER_SPACE);
		assertEquals(false, tracker.getSentNotAckedPackets().containsKey(AbstractWindow.SEQUENCE_NUMBER_SPACE - 10));
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(AbstractWindow.SEQUENCE_NUMBER_SPACE - 9));
	
		for (int i = 0; i < ReceivingWindow.RECEIVING_WINDOW_SIZE - 10; i++) {
			tracker.addPacket(i, packet0);
		}
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(AbstractWindow.SEQUENCE_NUMBER_SPACE - 1));
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(0));
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(3));
		tracker.updateSentPacketsList(2, AbstractWindow.SEQUENCE_NUMBER_SPACE - 10, AbstractWindow.SEQUENCE_NUMBER_SPACE);
		assertEquals(false, tracker.getSentNotAckedPackets().containsKey(AbstractWindow.SEQUENCE_NUMBER_SPACE - 1));
		assertEquals(false, tracker.getSentNotAckedPackets().containsKey(0));
		assertEquals(true, tracker.getSentNotAckedPackets().containsKey(3));
	}
	
}
