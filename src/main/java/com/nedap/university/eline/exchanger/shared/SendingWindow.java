package com.nedap.university.eline.exchanger.shared;

public class SendingWindow extends AbstractWindow {
	
	private int LFS = -1; //LastFrameSent
	private int LAR = -1; //LastAckknowledgementReceived
	private boolean ackRec = false;
	private int dACKs = 0;
	private int packetNumber = -1;
	
	public int getSWS() {
		return SWS;
	}
	
	public void incrementLFS() {
		synchronized (this) {
			this.LFS = (LFS + 1) % K;
		}
    }
	
	public int getLFS() {
		synchronized (this) {
			return LFS;
		}
	}
	
	public int getSubsequentLFS() {
		synchronized (this) {
			return ((LFS + 1) % K);
		}
	}
	
	public int getLAR() {
		synchronized (this) {
			return LAR;
		}
	}
	
	public void setLAR(final int aNumber) {
		synchronized (this) {
			LAR = aNumber;
		}
	}
	
	public boolean getAckRec() {
		synchronized (this) {
			return ackRec;
		}
	}
	
	public void setAckRec(final boolean aBoolean) {
		synchronized (this) {
			ackRec = aBoolean;
		}
	}
	
	public void incrementDACKs() {
		synchronized (this) {
			dACKs++;
		}
	}
	
	public void setDACKsToZero() {
		synchronized (this) {
			dACKs = 0;
		}
	}
	
	public int getDACKs() {
		synchronized (this) {
			return dACKs;
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
}
