package com.nedap.university.eline.exchanger.executor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nedap.university.eline.exchanger.manager.FileSendManager.sendReason;

public class FilePacketSender {
	
	private DatagramSocket socket;
	private SentPacketTracker packetTracker;
	
	public FilePacketSender(final DatagramSocket socket, final SentPacketTracker packetTracker) {
		this.socket = socket;
		this.packetTracker = packetTracker;
	}
	
	public void sendPacket(final DatagramPacket packet, final sendReason reason, final int seqNumber) {
    	try {
			socket.send(packet);
			setPacketTimer(seqNumber);
			packetTracker.addPacket(seqNumber, packet);
			if (reason == sendReason.PRIMARY) {
				System.out.println("Sent packet with seqNumber " + seqNumber + " for the first time.");
			} else if (reason == sendReason.DACK) {
				System.out.println("Sent packet with seqNumber " + seqNumber + " again after receiving 3+ DAcks.");
			} else if (reason == sendReason.TIMER) {
				System.out.println("Sent packet with seqNumber " + seqNumber + " again because the timer expired.");
			}
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
	
    public void ifNotAckedSendAgain(final int seqNumber) {
    	synchronized(packetTracker) {
	    	if(packetTracker.isAcked(seqNumber)) {
	    		DatagramPacket packet = packetTracker.getPreviouslySentPacket(seqNumber);
	    		packetTracker.removePacket(seqNumber);
	    		sendPacket(packet, sendReason.TIMER, seqNumber);
	    	}
    	}
    }
}
