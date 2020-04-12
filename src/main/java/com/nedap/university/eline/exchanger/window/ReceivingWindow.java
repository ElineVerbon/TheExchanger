package com.nedap.university.eline.exchanger.window;

public class ReceivingWindow extends AbstractWindow {
	
	private int largestConsecutivePacketReceived = -1; //LastFrameReceived = last consecutive frame
	
	
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
			if(newlargestConsecutivePacket < 0 || newlargestConsecutivePacket >= SEQUENCE_NUMBER_SPACE) {
				throw new IllegalArgumentException();
			}
			largestConsecutivePacketReceived = newlargestConsecutivePacket;
		}
	}
	
	public boolean isInWindow(final int aSeqNum) {
		return super.isInWindow(largestConsecutivePacketReceived, aSeqNum, "RWS");
	}
}
