package com.nedap.university.eline.exchanger.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.Receiver;
import com.nedap.university.eline.exchanger.executor.SentFilePacketTracker;
import com.nedap.university.eline.exchanger.packet.AckPacketContents;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class FileSendManager {
    
	private SendingWindow sendingWindow;
	private SentFilePacketTracker packetTracker;
	private FilePacketMaker filePacketMaker;
	private FilePacketSender filePacketSender;
	private Receiver receiver;
	
	//TODO make not public?
	public enum sendReason { PRIMARY, DACK, TIMER }
	
    public FileSendManager(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
		this.sendingWindow= new SendingWindow();
		this.packetTracker = new SentFilePacketTracker();
		this.filePacketMaker = new FilePacketMaker(bytes, destAddress, destPort);
		this.filePacketSender = new FilePacketSender(socket, packetTracker);
		this.receiver = new Receiver(socket);
    }
    
	public void sendFile() { //TODO rename
		
		new Thread(() -> sendPackets()).start();
		
		new Thread(() -> checkAcks()).start();
		
		//TODO do some checking here?
    }
	
	public void sendPackets() {
		boolean noMorePackets = false;
		
		while(!noMorePackets) {
			boolean inWindow = false;
			
			synchronized(sendingWindow) {
				giveUpdateToUser();
				inWindow = sendingWindow.isInWindow(sendingWindow.getSeqNumOneGreaterThanLastSent());
	
				if (inWindow) {
					sendingWindow.incrementLastFrameSent();
					sendingWindow.incrementPacketNumber();
					
					Optional<DatagramPacket> packet = filePacketMaker.makeDataPacket(sendingWindow.getPacketNumber(), sendingWindow.getLastFrameSent());
					if (packet == null) {
						noMorePackets = true;
					} else {
						filePacketSender.sendFilePacket(packet.get(), sendReason.PRIMARY, sendingWindow.getLastFrameSent());
					}
				} 
			}
			
			if (!inWindow) {
				waitABit();
			}
		}
	}
	
	public void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Transmitter at sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
		}
	}
	
    public void checkAcks() {
    	boolean lastAck = false;
    	while (!lastAck) {
    		DatagramPacket packet = receiver.receivePacket(AckPacketContents.getAckPacketLength());
    		AckPacketContents contents = new AckPacketContents(packet);
    		lastAck = contents.isAckOfLastPacket();
			processAck(contents.getSeqNum());
	    } 
    	System.out.println("File was successfully uploaded!");
    }

    public void processAck(final int seqNumber) {
    	System.out.print("Ack with seqNumber " + seqNumber + " received. ");
    	
    	synchronized(sendingWindow) {
    		System.out.println("Current LAR is " + sendingWindow.getLastAckknowledgementReceived());
			if (seqNumber == (sendingWindow.getLastAckknowledgementReceived())) {
				processDuplicateAck(seqNumber);
			} else if (sendingWindow.isInWindow(seqNumber)){
				processNewAck(seqNumber);
			} 
			
		}
    }
    
    public void processNewAck(final int seqNumber) {
    	sendingWindow.setDuplicateACKsToZero();
    	packetTracker.updateSentPacketsList(seqNumber, sendingWindow.getLastAckknowledgementReceived(), SendingWindow.getSequenceNumberSpace());
		sendingWindow.setLastAckknowledgementReceived(seqNumber);
    }

    
    public void processDuplicateAck(final int seqNumber) {
    	sendingWindow.incrementDuplicateACKs();
		if (sendingWindow.getDuplicateACKs() == 3) {
			sendingWindow.setDuplicateACKsToZero();
			filePacketSender.ifNotAckedSendAgain(seqNumber);
		}
    }
    
    public void giveUpdateToUser() {
    	if(sendingWindow.getSeqNumOneGreaterThanLastSent() == (SendingWindow.getSequenceNumberSpace()-1)) {
			//TODO add name of file, maybe change this to some other way to track status
		}
    }
}
