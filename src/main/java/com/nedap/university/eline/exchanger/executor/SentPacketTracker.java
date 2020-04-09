package com.nedap.university.eline.exchanger.executor;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

import com.nedap.university.eline.exchanger.manager.FileSendManager.sendReason;

public class SentPacketTracker {
	
	private Map<Integer, DatagramPacket> sentNotAckedPackets;
	
	public SentPacketTracker() {
		sentNotAckedPackets = new HashMap<>();
	}
	
	public Map<Integer, DatagramPacket> getSentNotAckedPackets() {
		synchronized(this ) {
			return sentNotAckedPackets;
		}
	}
	
	public boolean isAcked(final int expiredSeqNumber) {
		synchronized(this ) {
			return sentNotAckedPackets.containsKey(expiredSeqNumber);
		}
	}
	
    public void removePacket(final int seqNumber) {
    	synchronized(this ) {
    		sentNotAckedPackets.remove(seqNumber);
    	}
    }
    
    public void addPacket(final int seqNumber, DatagramPacket packet) {
    	synchronized(this) {
    		sentNotAckedPackets.put(seqNumber, packet);
    	}
    }
    
    public DatagramPacket getPreviouslySentPacket(final int seqNumber) {
    	synchronized(this ) {
    		return sentNotAckedPackets.get(seqNumber);
    	}
	}
	
    public void updateSentPacketsList(final int ackedSeqNum, final int LAR, final int K) {
    	synchronized(this) {
	    	if (ackedSeqNum > LAR) {
		    	for (int i = LAR + 1; i <= ackedSeqNum; i++) {
		    		if (sentNotAckedPackets.containsKey(ackedSeqNum)) {
		    			sentNotAckedPackets.remove(ackedSeqNum);
		    		}
		    	}
	    	} else {
	    		for (int i = LAR + 1; i <= (K + 1); i++) {
		    		if (sentNotAckedPackets.containsKey(ackedSeqNum)) {
		    			sentNotAckedPackets.remove(ackedSeqNum);
		    		}
		    	}
	    		for (int i = 0; i <= ackedSeqNum; i++) {
	    			if (sentNotAckedPackets.containsKey(ackedSeqNum)) {
		    			sentNotAckedPackets.remove(ackedSeqNum);
		    		}
	    		}
	    	}
    	}
    }
}
