package com.nedap.university.eline.exchanger.window;

public abstract class AbstractWindow {

    static final int SWS = 750; //TODO make it possible to change this depending on time out / DACK occurence
    static final int K = 2000;
    static final int RWS = 750;
    
    public int getK() {
		return K;
	}
    	
    public int seqNumToAckNum(final int seqNumber, final int lastAck, final int lastSeqNumber) {
    	final int seqRound = (seqNumber >= lastSeqNumber) ? lastAck / K : lastAck / K + 1;
    	return seqNumber + (seqRound * K);
    }
	
	public boolean isInWindow(int startWindow, int aNumber, String windowType) {
		int windowSize = (windowType == "SWS") ? SWS : RWS;
		int upperboundSW = (windowSize + startWindow) % K;
		int lowerboundSW = startWindow;
		
		if((windowSize + startWindow) / K == 0) {
			return ((aNumber > lowerboundSW && aNumber <= upperboundSW) ? true : false);
		} else {
			return ((aNumber >= 0 && aNumber <= upperboundSW) || 
					((aNumber > lowerboundSW && aNumber < K))
					? true : false);
		}
	}
}
