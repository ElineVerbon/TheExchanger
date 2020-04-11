package com.nedap.university.eline.exchanger.window;

public class ReceivingWindow extends AbstractWindow {
	
	private int largestAcceptablePacket;
	private int largestConsecutivePacketReceived = -1; //LastFrameReceived = last consecutive frame
	
	public ReceivingWindow() {
		setLargestAcceptablePacket();
	}
	
	public int getReceivingWindowSize() {
		return RECEIVING_WINDOW_SIZE;
	}
	
	public void incrementLargestConsecutivePacketReceived() {
		synchronized (this) {
			this.largestConsecutivePacketReceived = (largestConsecutivePacketReceived + 1) % SEQUENCE_NUMBER_SPACE;
		}
    }
	
	public int getLargestConsecutivePacketReceived() {
		synchronized (this) {
			return largestConsecutivePacketReceived;
		}
	}
	
	public int getSubsequentLargestConsecutivePacketReceived() {
		synchronized (this) {
			return ((largestConsecutivePacketReceived + 1) % SEQUENCE_NUMBER_SPACE);
		}
	}
	
	public void setLargestConsecutivePacketReceived(final int newlargestConsecutivePacket) {
		//TODO throw error when not in sequenceNumberSpace!
		synchronized (this) {
			largestConsecutivePacketReceived = newlargestConsecutivePacket;
		}
	}
	
	public int getLargestAcceptablePacket() {
		synchronized (this) {
			return largestAcceptablePacket;
		}
	}
	
	public void setLargestAcceptablePacket() {
		synchronized (this) {
			largestAcceptablePacket = RECEIVING_WINDOW_SIZE + largestConsecutivePacketReceived;
		}
	}
	
	public boolean isInWindow(final int aSeqNum) {
		return super.isInWindow(largestConsecutivePacketReceived, aSeqNum, "RWS");
	}
}
