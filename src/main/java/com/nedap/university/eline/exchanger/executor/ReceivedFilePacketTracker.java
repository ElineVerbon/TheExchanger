package com.nedap.university.eline.exchanger.executor;

import java.util.Map;
import java.util.TreeMap;

public class ReceivedFilePacketTracker {
	
	private Map<Integer, byte[]> receivedPackets = new TreeMap<>();
	
	public ReceivedFilePacketTracker() {
		receivedPackets = new TreeMap<>();
	}
	
	public void savePacket(final byte[] dataBytes, final int packetNumber) {
		
		if (packetNumber < 0) {
			throw new IllegalArgumentException("PacketNumber must be higher than zero.");
		}
		
		if (!receivedPackets.containsKey(packetNumber)) {
			receivedPackets.put(packetNumber, dataBytes);
		} 
	}
	
	public boolean allPacketsUpToMostRecentlyArrivedPacketReceived() {
		boolean recAllPackets = true;
		int packetNumber = 0;
		for (Map.Entry<Integer, byte[]> entry : receivedPackets.entrySet()) {
			if (entry.getKey() != packetNumber) {
				return false;
			}
			packetNumber++;
		}
		return recAllPackets;
	}
	
	public boolean packetAlreadyReceived(final int packetNumber) {
		
		if (packetNumber < 0) {
			throw new IllegalArgumentException("PacketNumber must be higher than zero.");
		}
		
		return receivedPackets.containsKey(packetNumber);
	}
	
	public int getHighestConsAccepFilePacket() {
		int largestAcceptedPackedNumber = -1;
		for (int i = 0; i < receivedPackets.size(); i++) {
			if (packetAlreadyReceived(i)) {
				largestAcceptedPackedNumber = i;
			} else {
				return largestAcceptedPackedNumber;
			}
		}
		return largestAcceptedPackedNumber;
	}
	
	public Map<Integer, byte[]> getAllReceivedPackets() {
		return receivedPackets;
	}
}
