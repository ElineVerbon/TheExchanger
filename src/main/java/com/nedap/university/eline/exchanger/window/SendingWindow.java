package com.nedap.university.eline.exchanger.window;

public class SendingWindow extends AbstractWindow {
	
	private int lastFrameSent = -1; //LastFrameSent
	private int lastAckknowledgementReceived = -1; //LastAckknowledgementReceived
	private int duplicateACKs = 0;
	private int packetNumber = -1;
	
	public int getSendingWindowSize() {
		return SENDING_WINDOW_SIZE;
	}

	public void incrementLastFrameSent() {
		synchronized (this) {
			this.lastFrameSent = (lastFrameSent + 1) % SEQUENCE_NUMBER_SPACE;
		}
    }
	
	public int getLastFrameSent() {
		synchronized (this) {
			return lastFrameSent;
		}
	}
	
	public int getSeqNumOneGreaterThanLastSent() {
		synchronized (this) {
			return ((lastFrameSent + 1) % SEQUENCE_NUMBER_SPACE);
		}
	}
	
	public int getLastAckknowledgementReceived() {
		synchronized (this) {
			return lastAckknowledgementReceived;
		}
	}
	
	public void setLastAckknowledgementReceived(final int aNumber) {
		synchronized (this) {
			if(aNumber < 0 || aNumber >= SEQUENCE_NUMBER_SPACE) {
				throw new IllegalArgumentException();
			}
			lastAckknowledgementReceived = aNumber;
		}
	}
	
	
	public void incrementDuplicateACKs() {
		synchronized (this) {
			duplicateACKs++;
		}
	}
	
	public void setDuplicateACKsToZero() {
		synchronized (this) {
			duplicateACKs = 0;
		}
	}
	
	public int getDuplicateACKs() {
		synchronized (this) {
			return duplicateACKs;
		}
	}
	
	public void incrementPacketNumber() {
		synchronized (this) {
			packetNumber++;
		}
	}
	
	public int getPacketNumber() {
		synchronized (this) {
			return packetNumber;
		}
	}
	
	public boolean isInWindow(final int aSeqNum) {
		return super.isInWindow(lastAckknowledgementReceived, aSeqNum, "SWS");
	}
}
