package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.util.Arrays;

public class AckPacketContents {

	private static int ACK_PACKET_LENGTH = 2 + SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH;
	private boolean lastPacket;
	private boolean dAck;
	private int seqNumber;
	
	public AckPacketContents(final DatagramPacket packet) {
		final byte[] bytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		
		if(bytes.length != ACK_PACKET_LENGTH) {
			throw new IllegalArgumentException();
		}
		
		lastPacket = getBooleanFromByte(bytes[0] &0xFF);
		dAck = getBooleanFromByte(bytes[1] &0xFF);
		seqNumber = SequenceNumberCalculator.getSeqNumFromBytes(Arrays.copyOfRange(bytes, 2, ACK_PACKET_LENGTH));
	}
		
	public boolean getBooleanFromByte(final int i) {
		if (i == 0) {
			return false;
		} else if (i == 1) {
			return true;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public boolean isAckOfLastPacket() {
		return lastPacket;
	}
	
	public boolean isDAck() {
		return dAck;
	}
	
	public int getSeqNum() {
		return seqNumber;
	}
	
	public static int getAckPacketLength() {
		return ACK_PACKET_LENGTH;
	}
}
