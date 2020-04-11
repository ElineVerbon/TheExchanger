package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;

public class FilePacketMaker {
	
	private byte[] bytes;
	private InetAddress destAddress;
    private int destPort;
	
	public FilePacketMaker(final byte[] bytesToBeSend, final InetAddress destAddress, final int destPort) {
		this.bytes = bytesToBeSend;
		this.destAddress = destAddress;
    	this.destPort = destPort;
	}
	
	public Optional<DatagramPacket> makeDataPacket(final int packetNumber, final int LFS) {
		
		if((packetNumber * FilePacketContents.DATASIZE) >= bytes.length) {
			return null;
		}
		
		final int lastPacket = ((((packetNumber+1) * FilePacketContents.DATASIZE) >= bytes.length) ? 1 : 0);
		final byte[] packetBytes = makeByteArrayForPacket(bytes, packetNumber, LFS, lastPacket);
		
		DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
		
		return Optional.of(packet);
	}
	
	public static byte[] makeByteArrayForPacket(final byte[] bytes, final int packetNumber, 
			final int LFS, final int lastPacket) {
		byte[] header = makeHeader(LFS, lastPacket);
		byte[] body = makeBody(bytes, packetNumber);
		
		byte[] packetBytes = new byte[header.length + body.length];
		System.arraycopy(header, 0, packetBytes, 0, header.length);
		System.arraycopy(body, 0, packetBytes, header.length, body.length);
		return packetBytes;
	}

	public static byte[] makeHeader(final int LFS, final int lastPacket) {
		byte[] seqNumInBytes = SequenceNumberCalculator.turnSeqNumIntoBytes(LFS);
		
		byte[] headerBytes = new byte[FilePacketContents.HEADERSIZE];
		System.arraycopy(seqNumInBytes, 0, headerBytes, 0, SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH);
		headerBytes[SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH] = (byte) lastPacket;
		
		return headerBytes;
	}
	
	public static byte[] makeBody(final byte[] bytes, final int packetNumber) {
		System.out.println(packetNumber);
		int to = Math.min(packetNumber * FilePacketContents.DATASIZE + FilePacketContents.DATASIZE, bytes.length);
		return Arrays.copyOfRange(bytes, packetNumber * FilePacketContents.DATASIZE, to);
	}
}
