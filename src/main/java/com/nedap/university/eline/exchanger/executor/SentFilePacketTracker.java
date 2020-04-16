package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class SentFilePacketTracker {
	
	private Map<Integer, DatagramPacket> sentNotAckedPackets;
	
	public SentFilePacketTracker() {
		sentNotAckedPackets = new HashMap<>();
	}
	
	public Map<Integer, DatagramPacket> getSentNotAckedPackets() {
		synchronized(this ) {
			return sentNotAckedPackets;
		}
	}
    
    public void addPacket(final int seqNumber, DatagramPacket packet) {
    	if (seqNumber < 0) {
    		throw new IllegalArgumentException("SeqNumber cannot be less than zero.");
    	}
    	
    	synchronized(this) {
    		if (sentNotAckedPackets.containsKey(seqNumber)) {
    			System.out.println("Warning: overwriting existing seqNumber.");
    		}
    		sentNotAckedPackets.put(seqNumber, packet);
    	}
    }
    
    public DatagramPacket getPreviouslySentPacket(final int seqNumber) {
    	synchronized(this ) {
    		if(sentNotAckedPackets.containsKey(seqNumber)) {
    			return sentNotAckedPackets.get(seqNumber);
    		} else {
    			throw new IllegalArgumentException("Packet could not be found.");
    		}
    	}
	}
    
    public void removePacket(final int seqNumber) {
    	if (seqNumber < 0) {
    		throw new IllegalArgumentException("SeqNumber cannot be less than zero.");
    	}
    	
    	synchronized(this ) {
    		if (sentNotAckedPackets.containsKey(seqNumber)) {
    			sentNotAckedPackets.remove(seqNumber);
    		} else {
    			throw new IllegalArgumentException("SeqNumber cannot be removed as it is not among saved seqNumbers.");
    		}
    		
    	}
    }
    
    public boolean hasSentPacketBeenAcked(final int expiredSeqNumber) {
		synchronized(this ) {
			return (!sentNotAckedPackets.containsKey(expiredSeqNumber));
		}
	}
	
    public void updateSentPacketsList(final int ackedSeqNum, 
    		final int previousLastAckknowledgementReceived, final int SeqNumRange) {
    	synchronized(this) {
	    	if (ackedSeqNum > previousLastAckknowledgementReceived) {
		    	for (int i = previousLastAckknowledgementReceived + 1; i <= ackedSeqNum; i++) {
		    		removePacketIfPresent(i);
		    	}
	    	} else {
	    		for (int i = previousLastAckknowledgementReceived + 1; i <= (SeqNumRange + 1); i++) {
	    			removePacketIfPresent(i);
		    	}
	    		for (int i = 0; i <= ackedSeqNum; i++) {
	    			removePacketIfPresent(i);
	    		}
	    	}
    	}
    }
    
    public void removePacketIfPresent(final int i) {
    	if (sentNotAckedPackets.containsKey(i)) {
			removePacket(i);
		}
    }
}
