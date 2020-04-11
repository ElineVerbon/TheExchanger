package com.nedap.university.eline.exchanger.window;

public abstract class AbstractWindow {

    static final int SENDING_WINDOW_SIZE = 750; //TODO make it possible to change this depending on time out / DACK occurence
    static final int SEQUENCE_NUMBER_SPACE = 2000;
    static final int RECEIVING_WINDOW_SIZE = 750;
    
    public int getSequenceNumberSpace() {
		return SEQUENCE_NUMBER_SPACE;
	}
    	
    public int seqNumToAckNum(final int seqNumber, final int lastAck, final int lastSeqNumber) {
    	final int seqRound = (seqNumber >= lastSeqNumber) ? lastAck / SEQUENCE_NUMBER_SPACE : lastAck / SEQUENCE_NUMBER_SPACE + 1;
    	return seqNumber + (seqRound * SEQUENCE_NUMBER_SPACE);
    }
	
	public boolean isInWindow(int startWindow, int aNumber, String windowType) {
		int windowSize = (windowType == "SWS") ? SENDING_WINDOW_SIZE : RECEIVING_WINDOW_SIZE;
		int upperboundSW = (windowSize + startWindow) % SEQUENCE_NUMBER_SPACE;
		int lowerboundSW = startWindow;
		
		if((windowSize + startWindow) / SEQUENCE_NUMBER_SPACE == 0) {
			return ((aNumber > lowerboundSW && aNumber <= upperboundSW) ? true : false);
		} else {
			return ((aNumber >= 0 && aNumber <= upperboundSW) || 
					((aNumber > lowerboundSW && aNumber < SEQUENCE_NUMBER_SPACE))
					? true : false);
		}
	}
}
