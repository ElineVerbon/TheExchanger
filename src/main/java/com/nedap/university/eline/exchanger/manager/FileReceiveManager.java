package com.nedap.university.eline.exchanger.manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class FileReceiveManager {
	
	private ReceivingWindow rws;
	private String windowType = "RWS";
	private Map<Integer, byte[]> receivedPackets = new TreeMap<>();
	private boolean recLastPacket = false;
	private boolean recAllPackets = false;
	private int[] lastAckedSeqNumPackPair = new int[] { 0, 0 };
	private int duplicateAck = 0;
	
	private DatagramSocket socket;
	private InetAddress srcAddress;
	private int srcPort;
	
	public FileReceiveManager(final DatagramSocket socket) {
		this.rws= new ReceivingWindow();
		this.socket = socket;
		rws.setLAF();
    }
	
	//this could be either the client or the server. what they do with it, depends on who is using this method.
	public byte[] receiveFile() {
		
		System.out.println("Receiving a file...");
		
		while(!recAllPackets) {
			final byte[] bytes = receivePacket();
			recLastPacket = (bytes[1] == 1);
			savePacketAndSendAck(bytes);
		}
		
		System.out.println("Received all the packets!");
		return collectAllBytes();
	}
	
	public byte[] receivePacket() {
		byte[] bytes = null;
		
		try {
			byte[] buffer = new byte[rws.getHeaderSize() + rws.getDataSize()];
		    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			srcAddress = packet.getAddress();
			srcPort = packet.getPort();
			bytes = packet.getData();
			bytes = Arrays.copyOfRange(bytes, 0, packet.getLength());
		} catch (IOException e) {
			System.out.println("Could not receive packet. Error message: " + e.getMessage());
		}
		
		return bytes;
	}
	
	public void savePacketAndSendAck(final byte[] bytes) {
		int seqNumber = bytes[0] &0xFF; //TODO make getSeqNumber in Header class (first make header class)
		
		if(seqNumber == (rws.getK() - 1)) {
			//TODO add name of file
			System.out.println("Still working on receiving the file! Received about 256 packets since last message");
		}
		
		if(!rws.isInWindow(rws.getLFR(), seqNumber, windowType)) {
			sendDuplicateAck();
			return;
		}
		
		final int packetNumber = getPacketNumber(seqNumber);
		if (seqNumber != rws.getSubsequentLFR()) {
			sendDuplicateAck();
			savePacket(packetNumber, bytes);
		} else {
			savePacket(packetNumber, bytes);
			setLFRToHighestConsAck(packetNumber);
			sendAck();
			lastAckedSeqNumPackPair = new int[] { seqNumber, packetNumber };
		}
	}
	
	public void sendDuplicateAck() {
		duplicateAck = 1;
		sendAck();
		duplicateAck = 0;
	}
	
	public void sendAck() {
		if(recLastPacket) {
			int packet = 0;
			for(Map.Entry<Integer, byte[]> entry : receivedPackets.entrySet()) {
				recAllPackets = true;
				if (entry.getKey() != packet) {
					recAllPackets = false;
					break;
				}
				packet++;
			}
		}
		int lastPacket = (recAllPackets ? 1 : 0);
		sendPacket(new byte[] { (byte) lastPacket, (byte) duplicateAck, (byte) rws.getLFR() }, srcAddress, srcPort, socket);
	}
	
	public int getPacketNumber(final int seqNumber) {
		//TODO there should be a nicer way to fix this (to not have to check this)
		int packetNumber;
		if(rws.getLFR() == -1) {
			packetNumber = seqNumber;
			return packetNumber;
		} else {
			//this is the last know seqnum / packetnum pair. This new packetNum can be at most RWS packets higher, cannot be lower (else not in window)
			int lastSeenSeqNumber = lastAckedSeqNumPackPair[0];
			int lastSeenPacketNumber = lastAckedSeqNumPackPair[1];
			
			if (seqNumber > lastSeenSeqNumber) {
				packetNumber = lastSeenPacketNumber + (seqNumber - lastSeenSeqNumber);
			} else {
				packetNumber = lastSeenPacketNumber + (rws.getK() - lastSeenSeqNumber + seqNumber);
			}
			
			return packetNumber;
		}
	}
	
	public void savePacket(final int packetNumber, final byte[] bytes) {
		if(!receivedPackets.containsKey(packetNumber)) {
			receivedPackets.put(packetNumber, Arrays.copyOfRange(bytes, rws.getHeaderSize(), bytes.length));
			//TODO there has to be a nicer way to check whether I received all previous packets
			
		}
	}
	
	public void sendPacket(byte[] bytes, InetAddress address, int port, DatagramSocket socket) {
		try {
			socket.send(new DatagramPacket(bytes, bytes.length, address, port));
		} catch (IOException e) {
			System.out.println("Could not send packet to " + address + ". Error message : " + e.getMessage());
		}
	}
	
	public void setLFRToHighestConsAck(final int packetNumber) {
		if(packetNumber == 0) {
			rws.setLFR(0);
		} else {
			for(int i = lastAckedSeqNumPackPair[1] + 1; i <= lastAckedSeqNumPackPair[1] + rws.getRWS(); i++) {
				if(receivedPackets.containsKey(i)) {
					rws.setLFR(i%rws.getK());
					rws.setLAF();
				} else {
					return;
				}
			}
		}
	}
	
	public byte[] collectAllBytes() {
		byte[] allBytes = {};
		
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
