package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import com.nedap.university.eline.exchanger.packet.AckPacketContents;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class AckReceiver extends Receiver {
	
	private SentFilePacketTracker packetTracker;
	private SendingWindow sendingWindow;
	private FilePacketSender filePacketSender;
	
	public AckReceiver(final DatagramSocket socket, final SentFilePacketTracker packetTracker, 
			final SendingWindow sendingWindow, FilePacketSender filePacketSender) {
		super(socket);
		this.packetTracker = packetTracker;
		this.sendingWindow = sendingWindow;
		this.filePacketSender = filePacketSender;
	}
	
	public boolean receiveAndProcessAck() throws SocketTimeoutException {
		AckPacketContents contents = receiveAck();
		processAck(contents.getSeqNum());
		
		return contents.isAckOfLastPacket();
	}
	
	public AckPacketContents receiveAck() throws SocketTimeoutException {
		DatagramPacket packet = receivePacket(AckPacketContents.getAckPacketLength());
		AckPacketContents contents = new AckPacketContents(packet);
		return contents;
	}
	
	public void processAck(final int seqNumber) {
		boolean isDuplicateAck = isDuplicateAck(seqNumber);
		if(isDuplicateAck) {
			processDuplicateAck(seqNumber);
		} else {
			processNewAck(seqNumber);
		}
	}
	
	public boolean isDuplicateAck(final int seqNumber) {
//    	System.out.println("Ack with seqNumber " + seqNumber + " received. ");
		if (seqNumber == (sendingWindow.getLastAckknowledgementReceived())) {
			return true;
		} else {
			return false;
		}
    }
    
    public void processNewAck(final int seqNumber) {
    	synchronized(sendingWindow) {
	    	sendingWindow.setDuplicateACKsToZero();
	    	packetTracker.updateSentPacketsList(seqNumber, sendingWindow.getLastAckknowledgementReceived(), SendingWindow.SEQUENCE_NUMBER_SPACE);
			sendingWindow.setLastAckknowledgementReceived(seqNumber);
    	}
    }

    
    public void processDuplicateAck(final int seqNumber) {
    	synchronized(sendingWindow) {
	    	sendingWindow.incrementDuplicateACKs();
			if (sendingWindow.getDuplicateACKs() == 3) {
				sendingWindow.setDuplicateACKsToZero();
				filePacketSender.ifNotAckedSendAgain(seqNumber);
			}
    	}
    }
}
