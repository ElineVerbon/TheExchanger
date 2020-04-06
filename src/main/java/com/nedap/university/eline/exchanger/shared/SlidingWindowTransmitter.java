package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SlidingWindowTransmitter extends AbstractSlidingWindowPlayer {
    
    private byte[] bytesToBeSend;
    private InetAddress destAddress;
    private int destPort;
    private Map<Integer, DatagramPacket> sentNotAckedPackets = new HashMap<>();
    private DatagramSocket socket;
	private int lastAckReceived = -1;
	private int lastSeqNumReceived = -1;
	private int lastPacketSent = -1;
	private int duplicateAcks = 0;
	private int seqNumber = 0;
	private boolean done;
	
    public SlidingWindowTransmitter(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
		this.bytesToBeSend = bytes;
    	this.destAddress = destAddress;
    	this.destPort = destPort;
    	this.socket = socket;
    }
    
	public void uploadFile() {
		System.out.println("starting to upload :)");
		
		System.out.println("final packetNumber: " + ((int) Math.ceil(bytesToBeSend.length / DATASIZE)));
		
		while(lastAckReceived != ((int) Math.ceil(bytesToBeSend.length / DATASIZE) - 1)) {
			while(SWS - lastAckReceived > 0 && !done) {
				System.out.println(lastAckReceived);
				System.out.println(lastPacketSent);
				sendPacket();
				//TODO: add timeout
			}
			checkAcks();
		}
		System.out.println("sent them all!");
    }
	
	public void sendPacket() {
		try {
			final DatagramPacket packet = makePacket();
			socket.send(packet);
			lastPacketSent++;
			sentNotAckedPackets.put(seqNumber, packet);
			seqNumber = (seqNumber < (K-1)) ? seqNumber + 1 : 0;
    	} catch (IOException e) {
    		System.out.println("Packet could not be sent, error message: " + e.getMessage());
		}
	}
	
	public DatagramPacket makePacket() {
		int packetNumber = lastPacketSent + 1;
		
		//set header
		int lastPacket = ((((packetNumber + 1) * DATASIZE) >= bytesToBeSend.length) ? 1 : 0);
		byte[] headerBytes = { (byte) seqNumber, (byte) lastPacket };
		
		//set rest
		if (lastPacket == 1) {
			done = true;
		}
		byte[] dataBytes = Arrays.copyOfRange(bytesToBeSend, packetNumber * DATASIZE, 
				Math.min((lastPacketSent + 1 ) * DATASIZE + DATASIZE, bytesToBeSend.length));
		byte[] packetBytes = new byte[dataBytes.length + headerBytes.length];
		System.arraycopy(headerBytes, 0, packetBytes, 0, HEADERSIZE);
		System.arraycopy(dataBytes, 0, packetBytes, HEADERSIZE, dataBytes.length);

		return new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
	}
    
    public void checkAcks() {
    	
        try {
        	byte[] buffer = new byte[4];
        	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
			socket.receive(response);
			final int seqNumber = ByteBuffer.wrap(response.getData()).getInt();
			
			System.out.println(seqNumber);
			
			if (seqNumber == (lastAckReceived%K)) {
				duplicateAcks++;
				if (duplicateAcks == 3) {
					socket.send(sentNotAckedPackets.get(seqNumber));
				}
			} else {
				duplicateAcks = 0;
				final int ackNumber = seqNumToAckNum(seqNumber, lastAckReceived, lastSeqNumReceived);
				lastAckReceived = ackNumber;
				lastSeqNumReceived = seqNumber;
				sentNotAckedPackets.remove(seqNumber);
				
				System.out.println("For debugging: Ack with seqNumber " + seqNumber + 
	    				" and ackNumber "+ ackNumber + "received!");
			}
		} catch (IOException e) {
			System.out.println("Acks could not be received or a packet could not be retransmitted, error message: " + e.getMessage());
		}
    }
}
