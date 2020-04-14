package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.util.Arrays;

public class FilePacketContents extends AbstractPacketContents {
	
	public final static int HEADERSIZE = SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH + 1;
	public final static int DATASIZE = 2500;
	private byte[] bytes;
	private int seqNumber;
	private boolean lastPacket;
	private byte[] dataBytes;
	
	public FilePacketContents(final DatagramPacket packet) {
		bytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		
		if(!(bytes.length > HEADERSIZE)) {
			throw new IllegalArgumentException();
		}
		
		final byte[] headerBytes = Arrays.copyOf(bytes, HEADERSIZE);
		final byte[] seqBytes = Arrays.copyOf(headerBytes, SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH);
		
		seqNumber = SequenceNumberCalculator.getSeqNumFromBytes(seqBytes);
		lastPacket = getBooleanFromInt(headerBytes[SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH]);
		dataBytes = Arrays.copyOfRange(bytes, HEADERSIZE, bytes.length);
	}
	
	public byte[] getDataBytes() {
		return dataBytes;
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public int getSeqNum() {
		return seqNumber;
	}
	
	public boolean isLastPacket() {
		return lastPacket;
	}
}
