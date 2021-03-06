package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class AckPacketMaker {
	
	private InetAddress destAddress;
    private int destPort;
    
	public AckPacketMaker(final InetAddress destAddress, final int destPort) {
		this.destAddress = destAddress;
    	this.destPort = destPort;
	}
	
	public DatagramPacket makePacket(final boolean recAllPackets, final boolean duplicateAck, final int LFR) {
		final int lastPacket = recAllPackets ? 1 : 0;
		final int dAck = duplicateAck ? 1 : 0;
		final byte[] seqBytes = SequenceNumberCalculator.turnSeqNumIntoBytes(LFR);
		
		final byte[] bytes = new byte[AckPacketContents.getAckPacketLength()];
		bytes[0] = (byte) lastPacket;
		bytes[1] = (byte) dAck;
		System.arraycopy(seqBytes, 0, bytes, 2, SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH);
		Arrays.copyOfRange(seqBytes, 2, AckPacketContents.getAckPacketLength());
		
		return new DatagramPacket(bytes, bytes.length, destAddress, destPort);
	}
}
