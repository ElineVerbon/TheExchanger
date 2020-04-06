package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class SlidingWindowReceiver extends AbstractSlidingWindowPlayer {
	
	private DatagramSocket socket;
	private InetAddress srcAddress;
	private int srcPort;
	private int LAF;
	private int LFR = -1;
	private int lastSeqNumberAcked = -1;
	private Map<Integer, byte[]> receivedPackets = new TreeMap<>();

	//for debugging
	private int numberBytesReceived = 0;
	
	public SlidingWindowReceiver(final DatagramSocket socket) {
		this.socket = socket;
		LAF = RWS + LFR; //i.e. RWS - 1
    }
	
	//this could be either the client or the server. what they do with it, depends on who is using this method.
	public byte[] receiveFile() {
		System.out.println("starting to receive :)");
		
		boolean done = false;
		
		while(!done) {
			final byte[] bytes = receivePacket();
			
			//for debugging
			int newBytes = bytes.length;
			numberBytesReceived = numberBytesReceived + (newBytes - HEADERSIZE);
			System.out.println("Number of newly received bytes = " + newBytes);
			System.out.println("Total number bytes of file = " + numberBytesReceived);
			
			savePacketAndSendAck(bytes);
			done = (bytes[1] == 1);
			//TODO need to wait two roundtriptimes to see if ack was lost and new needs to be send?
		}
		
		System.out.println("received them all!");
		return collectAllBytes();
	}
	
	public byte[] receivePacket() {
		byte[] bytes = null;
		
		System.out.println("receiving one packet");
		
		try {
			byte[] buffer = new byte[HEADERSIZE + DATASIZE];
		    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			srcAddress = packet.getAddress();
			srcPort = packet.getPort();
			bytes = packet.getData();
		} catch (IOException e) {
			System.out.println("Could not receive packet. Error message: " + e.getMessage());
		}
		
		return bytes;
	}
	
	public void savePacketAndSendAck(byte[] bytes) {
		int seqNumber = bytes[0]; //TODO make getSeqNumber in Header class (first make header class)
		System.out.println(seqNumber);
		
		if(!seqNumberInRWS(seqNumber)) {
			sendDuplicateAck();
			return;
		}

		final int packetNumber = getPacketNumber(seqNumber);
		System.out.println("packet number = " + packetNumber);
		if (packetNumber != LFR + 1) {
			sendDuplicateAck();
			savePacket(packetNumber, bytes);
		} else {
			savePacket(packetNumber, bytes);
			
			findLargestConsecutivePacketNumberReceived().ifPresentOrElse(
					packetNum -> sendPacket(new byte[] { (byte) (packetNumber % K) }, srcAddress, srcPort, socket),
					() -> System.out.println("Something went wrong while searching for the highest consecutive ack. PacketNumber = " + packetNumber));
		}
	}
	
	public boolean seqNumberInRWS(final int seqNumber) {
		
		final int seqNumSlotsAvailableAtEnd = (K - 1) - LFR;
		if (seqNumSlotsAvailableAtEnd >= RWS) {
			if (seqNumber > LFR && seqNumber <= LFR + 4) {
				return true;
			} else {
				return false;
			}
		} else {
			if ((seqNumber > LFR && seqNumber <= (K - 1)) || seqNumber >= 0 && seqNumber <= (RWS - seqNumSlotsAvailableAtEnd)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public void sendDuplicateAck() {
		byte[] duplicateAck = { (byte) lastSeqNumberAcked };
		sendPacket(duplicateAck, srcAddress, srcPort, socket);
	}
	
	public int getPacketNumber(final int seqNumber) {
		//TODO there should be a nicer way to fix this (to not have to check this)
		return (LFR > -1) ? seqNumToAckNum(seqNumber, lastSeqNumberAcked, lastSeqNumberAcked) : seqNumber;
	}
	
	public void savePacket(final int ackNumber, final byte[] bytes) {
		System.out.println("(in savePacket()) packet number = " + ackNumber);
		if(!receivedPackets.containsKey(ackNumber)) {
			System.out.println("Values in receivedPackets = " + receivedPackets.size());
			receivedPackets.put(ackNumber, Arrays.copyOfRange(bytes, HEADERSIZE, bytes.length));
			System.out.println("Number bytes saved = " + receivedPackets.get(ackNumber).length);
			System.out.println("");
		}
	}
	
	public Optional<Integer> findLargestConsecutivePacketNumberReceived() {
		for(int i = LFR + RWS; i >= LFR + 1; i--) {
			if(receivedPackets.containsKey(i)) {
				LFR = i;
				LAF = LFR + RWS;
				return Optional.of(i);
			}
		}
		return null;
	}
	
	public byte[] collectAllBytes() {
		byte[] allBytes = {};
		
		int numberBytesToFile = 0;
		int num = 0;
		System.out.println("Number of entries in the map = " + receivedPackets.size());
		
		for(Map.Entry<Integer, byte[]> entry : receivedPackets.entrySet()) {
			byte[] toBeAddedBytes = entry.getValue();
			byte[] newAllBytes = new byte[allBytes.length + toBeAddedBytes.length];
			System.arraycopy(allBytes, 0, newAllBytes, 0, allBytes.length);
			System.arraycopy(toBeAddedBytes, 0, newAllBytes, allBytes.length, toBeAddedBytes.length);
			allBytes = newAllBytes;
			//TODO: make into something like below
			//receivedPackets.entrySet().stream().map(element -> element.getValue()).collect(collector)
		}
		return allBytes;
	}
}
