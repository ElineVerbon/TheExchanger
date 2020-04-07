package com.nedap.university.eline.exchanger.shared;

abstract class AbstractWindow {

	static final int HEADERSIZE = 2;
	static final int DATASIZE = 512;
    static final int SWS = 50; //TODO make it possible to change this depending on time out / DACK occurence
    static final int K = 256;
    static final int RWS = 50;
    
    public int getDataSize() {
    	return DATASIZE;
    }
    
    public int getHeaderSize() {
    	return HEADERSIZE;
    }
    	
    public int seqNumToAckNum(final int seqNumber, final int lastAck, final int lastSeqNumber) {
    	final int seqRound = (seqNumber >= lastSeqNumber) ? lastAck / K : lastAck / K + 1;
    	return seqNumber + (seqRound * K);
    }
	
	public boolean isInWindow(int startWindow, int aNumber, String windowType) {
		int windowSize = (windowType == "SWS") ? SWS : RWS;
		int upperboundSW = (windowSize + startWindow) % (K); //TODO or K - 1?
		int lowerboundSW = startWindow + 1;
		if((windowSize + startWindow) / (K - 1) == 0) {
			return ((aNumber <= upperboundSW && aNumber >= lowerboundSW) ? true : false);
		} else {
			return ((aNumber <= upperboundSW && aNumber >= 0) || 
					((aNumber >= lowerboundSW && aNumber < K))
					? true : false);
		}
	}
}
