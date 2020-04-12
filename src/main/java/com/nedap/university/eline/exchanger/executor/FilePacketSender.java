package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nedap.university.eline.exchanger.manager.FileSendManager.sendReason;

public class FilePacketSender extends AbstractSender{
	
	private SentFilePacketTracker packetTracker;
	public static final int timeOutTime = 2;
	
	public FilePacketSender(final DatagramSocket socket, final SentFilePacketTracker packetTracker) {
		super(socket);
		this.packetTracker = packetTracker;
	}
	
	public void sendFilePacket(final DatagramPacket packet, final sendReason reason, final int seqNumber) {
		sendPacket(packet);
		setPacketTimer(seqNumber);
		packetTracker.addPacket(seqNumber, packet);
		updateUser(reason, seqNumber);
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
     
        scheduler.schedule(task, timeOutTime, TimeUnit.SECONDS);
        scheduler.shutdown();
    }
	
    public void ifNotAckedSendAgain(final int seqNumber) {
    	synchronized(packetTracker) {
	    	if(packetTracker.isAcked(seqNumber)) {
	    		DatagramPacket packet = packetTracker.getPreviouslySentPacket(seqNumber);
	    		packetTracker.removePacket(seqNumber);
	    		sendFilePacket(packet, sendReason.TIMER, seqNumber);
	    	}
    	}
    }
    
    public void updateUser(final sendReason reason, final int seqNumber ) {
    	if (reason == sendReason.PRIMARY) {
			System.out.println("Sent packet with seqNumber " + seqNumber + " for the first time.");
		} else if (reason == sendReason.DACK) {
			System.out.println("Sent packet with seqNumber " + seqNumber + " again after receiving 3+ DAcks.");
		} else if (reason == sendReason.TIMER) {
			System.out.println("Sent packet with seqNumber " + seqNumber + " again because the timer expired.");
		}
    }
}
