package com.nedap.university.eline.exchanger.executor;

import java.util.Map;
import java.util.TreeMap;

public class ReceivedFilePacketTracker {
	
	private Map<Integer, byte[]> receivedPackets = new TreeMap<>();
	
	public ReceivedFilePacketTracker() {
		receivedPackets = new TreeMap<>();
	}
	
	public void savePacket(final byte[] dataBytes, final int packetNumber) {
		if(!receivedPackets.containsKey(packetNumber)) {
			receivedPackets.put(packetNumber, dataBytes);
		}
	}
	
	public boolean allPacketsReceived() {
		boolean recAllPackets = true;
		int packetNumber = 0;
		for(Map.Entry<Integer, byte[]> entry : receivedPackets.entrySet()) {
			if (entry.getKey() != packetNumber) {
				recAllPackets = false;
				break;
			}
			packetNumber++;
		}
		return recAllPackets;
	}
	
	public boolean packetAlreadyReceived(final int packetNumber) {
		return receivedPackets.containsKey(packetNumber);
	}
	
	public int getHighestConsAccepFilePacket(final int lastAckedPacketNumber, final int RWS) {
		int highestAcceptedFile = lastAckedPacketNumber;
		for(int i = lastAckedPacketNumber + 1; i <= (lastAckedPacketNumber + RWS); i++) {
			if(packetAlreadyReceived(i)) {
				highestAcceptedFile = i;
			} else {
				return highestAcceptedFile;
			}
		}
		return highestAcceptedFile;
	}
	
	public Map<Integer, byte[]> getAllReceivedPackets() {
		return receivedPackets;
	}
}
