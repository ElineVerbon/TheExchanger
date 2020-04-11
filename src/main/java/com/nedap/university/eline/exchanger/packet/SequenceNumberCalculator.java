package com.nedap.university.eline.exchanger.packet;

public abstract class SequenceNumberCalculator {
	
	public final static int SEQ_NUM_BYTE_LENGTH = 4;
	
	public static int getSeqNumFromBytes(final byte[] seqByte) {
		if(seqByte.length != 4) {
			throw new IllegalArgumentException();
		}
		return ((0xFF & seqByte[0]) << 24) | ((0xFF & seqByte[1]) << 16) 
				| ((0xFF & seqByte[2]) << 8) | (0xFF & seqByte[3]);
	}
	
	public static byte[] turnSeqNumIntoBytes(final int seqNum) {
		if (seqNum < 0) {
			throw new IllegalArgumentException();
		}
		return new byte[] { (byte) (seqNum >> 24), (byte) (seqNum >> 16),
	            (byte) (seqNum >> 8), (byte) seqNum };
	}
}
