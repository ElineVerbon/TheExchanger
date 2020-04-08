package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SendingCommunicator extends AbstractCommunicator {
    
	private SendingWindow sw;
	private SentPacketTracker packetTracker;
	
    private byte[] bytesToBeSend;
    private int lastPacket = 0;
	enum sendReason { PRIMARY, DACK, TIMER }
	
	//TODO pull this apart?
    private InetAddress destAddress;
    private int destPort;
    private DatagramSocket socket;
	
	
    public SendingCommunicator(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
		this.sw= new SendingWindow();
		this.packetTracker = new SentPacketTracker();
		this.bytesToBeSend = bytes;
    	this.destAddress = destAddress;
    	this.destPort = destPort;
    	this.socket = socket;
    	
    }
    
	public void uploadFile() {
		
		new Thread(() -> sendPackets()).start();
		
		new Thread(() -> checkAcks()).start();
		
		//TODO do some checking here?
    }
	
	public void sendPackets() {
		while(lastPacket == 0) {
			prepareAndSendPacket();
		}
	}
	
	public void prepareAndSendPacket() {
		boolean inWindow = false;
		
		synchronized(sw) {
			giveUpdateToUser();
			inWindow = sw.isInWindow(sw.getLAR(), sw.getSubsequentLFS(), "SWS");

			if(inWindow) {
				sendPacket(makePacket(), sendReason.PRIMARY, sw.getLFS());
			} 
		}
		
		if (!inWindow) {
			waitABit();
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

	
	public DatagramPacket makePacket() {
		sw.incrementLFS();
		sw.incrementPacketNumber();
		lastPacket = ((((sw.getPacketNumber()+1) * sw.getDataSize()) >= bytesToBeSend.length) ? 1 : 0);
		
		return makeDataPacket(makeHeader(), makeBody(), destAddress, destPort);
	}
	
	public void sendPacket(final DatagramPacket packet, final sendReason reason, final int seqNumber) {
    	try {
			socket.send(packet);
			setPacketTimer(seqNumber);
			packetTracker.addPacket(seqNumber, packet);
//			if (reason == sendReason.PRIMARY) {
//				System.out.println("Sent packet with seqNumber " + seqNumber + " for the first time.");
//			} else if (reason == sendReason.DACK) {
//				System.out.println("Sent packet with seqNumber " + seqNumber + " again after receiving 3+ DAcks.");
//			} else if (reason == sendReason.TIMER) {
//				System.out.println("Sent packet with seqNumber " + seqNumber + " again because the timer expired.");
//			}
		} catch (IOException e) {
			System.out.println("Packet could not be sent, error message: " + e.getMessage());
		}
    }
	
	public void setPacketTimer(final int seqNumber) {
    	ScheduledExecutorService scheduler
                                = Executors.newSingleThreadScheduledExecutor();
     
        Runnable task = new Runnable() {
            public void run() {
            	synchronized(packetTracker) {
            		ifNotAckedSendAgain(seqNumber);
            	}
            }
        };
     
        int delay = 5;
        scheduler.schedule(task, delay, TimeUnit.SECONDS);
        scheduler.shutdown();
    }
	
	public byte[] makeHeader() {
		return new byte[] { (byte) sw.getLFS(), (byte) lastPacket };
	}
	
	public byte[] makeBody() {
		int to = Math.min((sw.getPacketNumber()) * sw.getDataSize() + sw.getDataSize(), bytesToBeSend.length);
		return Arrays.copyOfRange(bytesToBeSend, sw.getPacketNumber() * sw.getDataSize(), to);
	}
    
    public void checkAcks() {
    	boolean lastAck = false;
    	while(!lastAck) {
        	DatagramPacket response = receivePacket(socket, 3);
        	lastAck = ((response.getData()[0] &0xFF) == 1) ? true : false;
			final int seqNumber = response.getData()[2] &0xFF;
//			System.out.println("Ack with seqNumber " + seqNumber + " received");
			
			processAck(seqNumber);
	    }
//    	System.out.println("File was successfully uploaded!");
    }

    public void processAck(final int seqNumber) {
    	synchronized(sw) {
			if (seqNumber == (sw.getLAR())) {
				processDuplicateAck(seqNumber);
			} else {
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
			ifNotAckedSendAgain(seqNumber);
		}
    }
    
    public void ifNotAckedSendAgain(final int seqNumber) {
    	synchronized(packetTracker) {
	    	if(packetTracker.isAcked(seqNumber)) {
	    		DatagramPacket packet = packetTracker.getPreviouslySentPacket(seqNumber);
	    		packetTracker.removePacket(seqNumber);
	    		sendPacket(packet, sendReason.TIMER, seqNumber);
	    	}
    	}
    }
    
    public void giveUpdateToUser() {
    	if(sw.getSubsequentLFS() == (sw.getK()-1)) {
			//TODO add name of file, maybe change this to some other way to track status
//			System.out.println("Still working on sending the file! Sent 256 packets since last message");
		}
    }
}
