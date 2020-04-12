package com.nedap.university.eline.exchanger.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

import com.nedap.university.eline.exchanger.executor.AckSender;
import com.nedap.university.eline.exchanger.executor.ReceivedFilePacketTracker;
import com.nedap.university.eline.exchanger.executor.Receiver;
import com.nedap.university.eline.exchanger.packet.FilePacketContents;
import com.nedap.university.eline.exchanger.packet.AckPacketMaker;
import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class FileReceiveManager {
	
	private ReceivingWindow receivingWindow;
	private ReceivedFilePacketTracker packetTracker;
	private Receiver receiver;
	private AckPacketMaker ackMaker;
	private AckSender ackSender;
	
	private int[] lastAckedSeqNumPacNumPair = new int[2];
	public boolean firstPacket = true;
	public boolean recAllPackets = false;
	private boolean recLastPacket = false;
	private boolean duplicateAck = false;
	
	public FileReceiveManager(final DatagramSocket socket) {
		this.receivingWindow= new ReceivingWindow();
		this.packetTracker = new ReceivedFilePacketTracker();
		receiver = new Receiver(socket);
		this.ackSender = new AckSender(socket);
    }
	
	public byte[] receiveFile() {
		
		System.out.println("Receiving a file...");
		
		while(!recAllPackets) {
			final DatagramPacket packet = receiver.receivePacket(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE);
			if(firstPacket) {
				this.ackMaker = new AckPacketMaker(packet.getAddress(), packet.getPort());
			}
			processPacket(new FilePacketContents(packet));
			firstPacket = false;
		}
		
		waitToVerifySenderHasReceivedAckAndIfNotSendAgain();
		
		System.out.println("Received all the packets!");
		return collectAllBytes();
	}
	
	public void waitToVerifySenderHasReceivedAckAndIfNotSendAgain() {
		System.out.println("waiting");
		final DatagramPacket possiblePacket = receiver.receivePacketWithTimeOut(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE);
		if(possiblePacket != null) {
			sendDuplicateAck();
			waitToVerifySenderHasReceivedAckAndIfNotSendAgain();
		}
	}
	
	public void processPacket(final FilePacketContents packet) {
		
		if(!receivingWindow.isInWindow(packet.getSeqNum())) {
			sendDuplicateAck();
			return;
		}
		
		final int packetNumber = getPacketNumber(packet.getSeqNum());
		
//		System.out.println("Received packet with seqNum " + packet.getSeqNum() + " and PacketNum " + packetNumber);
		
		packetTracker.savePacket(packet.getDataBytes(), packetNumber);
		if(packet.isLastPacket()) {
			recLastPacket = true;
		}
		
		if (packet.getSeqNum() != receivingWindow.getSubsequentLargestConsecutivePacketReceived()) {
			sendDuplicateAck();
		} else {
			setLFRToHighestConsAck(packetNumber);
			sendAck();
			lastAckedSeqNumPacNumPair = new int[] { packet.getSeqNum(), packetNumber };
		}
	}
	
	public int getPacketNumber(final int seqNumber) {
		int packetNumber;
		if(firstPacket) {
			packetNumber = seqNumber;
			return packetNumber;
		} else {
			int lastSeenSeqNumber = lastAckedSeqNumPacNumPair[0];
			int lastSeenPacketNumber = lastAckedSeqNumPacNumPair[1];
			
			if (seqNumber > lastSeenSeqNumber) {
				packetNumber = lastSeenPacketNumber + (seqNumber - lastSeenSeqNumber);
			} else {
				packetNumber = lastSeenPacketNumber + (ReceivingWindow.SEQUENCE_NUMBER_SPACE - lastSeenSeqNumber + seqNumber);
			}
			
			return packetNumber;
		}
	}
	
	public void sendDuplicateAck() {
		duplicateAck = true;
		sendAck();
		duplicateAck = false;
	}
	
	public void sendAck() {
//		System.out.println("sending an ack with seqnum " + rws.getLFR());
		recAllPackets = recLastPacket && packetTracker.allPacketsReceived();
		final DatagramPacket ack = ackMaker.makePacket(recAllPackets, duplicateAck, receivingWindow.getLargestConsecutivePacketReceived());
		ackSender.sendPacket(ack);
	}
	
	public void setLFRToHighestConsAck(final int packetNumber) {
		if(packetNumber == 0) {
			receivingWindow.setLargestConsecutivePacketReceived(0);
		} else {
			final int highestConPacketAccepted = packetTracker.getHighestConsAccepFilePacket(lastAckedSeqNumPacNumPair[1], receivingWindow.getReceivingWindowSize());
			receivingWindow.setLargestConsecutivePacketReceived(highestConPacketAccepted%ReceivingWindow.SEQUENCE_NUMBER_SPACE);
		}
	}
	
	public byte[] collectAllBytes() {
		byte[] allBytes = {};
		
		System.out.println("Collecting all bytes, please be patient!");
		
		for(Map.Entry<Integer, byte[]> entry : packetTracker.getAllReceivedPackets().entrySet()) {
			byte[] toBeAddedBytes = entry.getValue();
			byte[] newAllBytes = new byte[allBytes.length + toBeAddedBytes.length];
			System.arraycopy(allBytes, 0, newAllBytes, 0, allBytes.length);
			System.arraycopy(toBeAddedBytes, 0, newAllBytes, allBytes.length, toBeAddedBytes.length);
			allBytes = newAllBytes;
			
			System.out.println(allBytes.length);
			//TODO: make into something like below
			//receivedPackets.entrySet().stream().map(element -> element.getValue()).collect(collector)
		}
		System.out.println("Length of allBytes: " + allBytes.length);
		return allBytes;
	}
}
