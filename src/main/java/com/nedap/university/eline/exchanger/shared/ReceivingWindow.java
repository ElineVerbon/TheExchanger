package com.nedap.university.eline.exchanger.shared;

public class ReceivingWindow extends AbstractWindow {
	
	private int LAF; //LargestAcceptableFrame
	private int LFR = -1; //LastFrameReceived = last consecutive frame
	
	public int getRWS() {
		return RWS;
	}
	
	public void incrementLFR() {
		synchronized (this) {
			this.LFR = (LFR + 1) % K;
		}
    }
	
	public int getLFR() {
		synchronized (this) {
			return LFR;
		}
	}
	
	public int getSubsequentLFR() {
		synchronized (this) {
			return ((LFR + 1) % K);
		}
	}
	
	public void setLFR(final int newLFR) {
		synchronized (this) {
			LFR = newLFR;
		}
	}
	
	public int getLAF() {
		synchronized (this) {
			return LAF;
		}
	}
	
	public void setLAF() {
		synchronized (this) {
			LAF = RWS + LFR;
		}
	}
}
