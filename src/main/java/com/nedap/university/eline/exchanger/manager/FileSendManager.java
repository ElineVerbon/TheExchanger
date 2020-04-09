package com.nedap.university.eline.exchanger.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;

import com.nedap.university.eline.exchanger.executor.AckReceiver;
import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.SentPacketTracker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class FileSendManager {
    
	private SendingWindow sw;
	private SentPacketTracker packetTracker;
	private FilePacketMaker filePacketMaker;
	private FilePacketSender filePacketSender;
	private AckReceiver ackReceiver;
	
	//TODO make not public?
	public enum sendReason { PRIMARY, DACK, TIMER }
	
    public FileSendManager(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
		this.sw= new SendingWindow();
		this.packetTracker = new SentPacketTracker();
		this.filePacketMaker = new FilePacketMaker(bytes, destAddress, destPort, sw.getDataSize());
		this.filePacketSender = new FilePacketSender(socket, packetTracker);
		this.ackReceiver = new AckReceiver(socket);
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
			
			synchronized(sw) {
				giveUpdateToUser();
				inWindow = sw.isInWindow(sw.getLAR(), sw.getSubsequentLFS(), "SWS");
	
				if (inWindow) {
					sw.incrementLFS();
					sw.incrementPacketNumber();
					
					Optional<DatagramPacket> packet = filePacketMaker.makeDataPacket(sw.getPacketNumber(), sw.getLFS());
					if (packet == null) {
						noMorePackets = true;
					} else {
						filePacketSender.sendPacket(packet.get(), sendReason.PRIMARY, sw.getLFS());
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
    	
    	while(!ackReceiver.isDone()) {
    		final int seqNumber = ackReceiver.getAck();
			System.out.print("Ack with seqNumber " + seqNumber + " received. ");
			processAck(seqNumber);
	    }
    	System.out.println("File was successfully uploaded!");
    }

    public void processAck(final int seqNumber) {
    	synchronized(sw) {
    		System.out.println("Current LAR is " + sw.getLAR());
			if (seqNumber == (sw.getLAR())) {
				processDuplicateAck(seqNumber);
			} else if (sw.isInWindow(seqNumber)){
				processNewAck(seqNumber);
			} 
			sw.setAckRec(true);
		}
    }
    
    public void processNewAck(final int seqNumber) {
    	sw.setDACKsToZero();
    	packetTracker.updateSentPacketsList(seqNumber, sw.getLAR(), sw.getK());
		sw.setLAR(seqNumber);
    }

    
    public void processDuplicateAck(final int seqNumber) {
    	sw.incrementDACKs();
		if (sw.getDACKs() == 3) {
			sw.setDACKsToZero();
			filePacketSender.ifNotAckedSendAgain(seqNumber);
		}
    }
    
    public void giveUpdateToUser() {
    	if(sw.getSubsequentLFS() == (sw.getK()-1)) {
			//TODO add name of file, maybe change this to some other way to track status
//			System.out.println("Still working on sending the file! Sent 256 packets since last message");
		}
    }
}