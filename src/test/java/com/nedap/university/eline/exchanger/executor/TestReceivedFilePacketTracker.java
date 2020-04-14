package com.nedap.university.eline.exchanger.executor;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

public class TestReceivedFilePacketTracker {
	
	@Test
	public void testSaveAndRetrievePacket() {
		byte[] bytes0 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 };
		byte[] bytes1 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 };
		
		ReceivedFilePacketTracker tracker = new ReceivedFilePacketTracker();
		tracker.savePacket(bytes0, 0);
		assertEquals(true, tracker.packetAlreadyReceived(0));
		assertEquals(false, tracker.packetAlreadyReceived(1));
		
		assertThrows(IllegalArgumentException.class, () -> tracker.savePacket(bytes1, -1));
		assertThrows(IllegalArgumentException.class, () -> tracker.packetAlreadyReceived(-1));
	}
	
	@Test
	public void testAllPacketsUpToMostRecentlyArrivedPacketReceived() {
		byte[] bytes0 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 };
		byte[] bytes1 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 };
		byte[] bytes2 = new byte[] { (byte) 0b00000000, (byte) 0b00001000, (byte) 0b00110111, (byte) 0b11100001 };
		byte[] bytes3 = new byte[] { (byte) 0b01111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 };
		
		ReceivedFilePacketTracker tracker = new ReceivedFilePacketTracker();
		
		assertEquals(true, tracker.allPacketsUpToMostRecentlyArrivedPacketReceived());
		
		tracker.savePacket(bytes0, 0);
		tracker.savePacket(bytes1,  1);
		assertEquals(true, tracker.allPacketsUpToMostRecentlyArrivedPacketReceived());
		
		tracker.savePacket(bytes3, 3);
		assertEquals(false, tracker.allPacketsUpToMostRecentlyArrivedPacketReceived());
		
		tracker.savePacket(bytes2, 2);
		assertEquals(true, tracker.allPacketsUpToMostRecentlyArrivedPacketReceived());
	}
	
	@Test
	public void testGetHighestConsAccepFilePacket() {
		byte[] bytes0 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 };
		byte[] bytes1 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 };
		byte[] bytes2 = new byte[] { (byte) 0b00000000, (byte) 0b00001000, (byte) 0b00110111, (byte) 0b11100001 };
		byte[] bytes3 = new byte[] { (byte) 0b01111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 };
		
		ReceivedFilePacketTracker tracker = new ReceivedFilePacketTracker();
		
		assertEquals(-1, tracker.getHighestConsAccepFilePacket());
		
		tracker.savePacket(bytes0, 0);
		tracker.savePacket(bytes1,  1);
		assertEquals(1, tracker.getHighestConsAccepFilePacket());
		
		tracker.savePacket(bytes3, 3);
		assertEquals(1, tracker.getHighestConsAccepFilePacket());
		
		tracker.savePacket(bytes2, 2);
		assertEquals(3, tracker.getHighestConsAccepFilePacket());
	}
	
	@Test
	public void testGetAllReceivedPackets() {
		byte[] bytes0 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 };
		byte[] bytes1 = new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 };
		byte[] bytes2 = new byte[] { (byte) 0b00000000, (byte) 0b00001000, (byte) 0b00110111, (byte) 0b11100001 };
		byte[] bytes3 = new byte[] { (byte) 0b01111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 };
		
		ReceivedFilePacketTracker tracker = new ReceivedFilePacketTracker();
		tracker.savePacket(bytes0, 0);
		tracker.savePacket(bytes1, 1);
		tracker.savePacket(bytes3, 3);
		tracker.savePacket(bytes2, 2);
		
		Map<Integer, byte[]> receivedPackets = tracker.getAllReceivedPackets();
		assertEquals(bytes0, receivedPackets.get(0));
		assertEquals(bytes1, receivedPackets.get(1));
		assertEquals(bytes2, receivedPackets.get(2));
		assertEquals(bytes3, receivedPackets.get(3));
		assertNotEquals(bytes2, receivedPackets.get(3));
	}
}
