package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;

public class FilePacketMaker {
	
	private byte[] bytes;
	private InetAddress destAddress;
    private int destPort;
    private int dataSize;
	
	public FilePacketMaker(final byte[] bytesToBeSend, final InetAddress destAddress, final int destPort, final int dataSize) {
		this.bytes = bytesToBeSend;
		this.destAddress = destAddress;
    	this.destPort = destPort;
    	this.dataSize = dataSize;
	}
	
	public Optional<DatagramPacket> makeDataPacket(final int packetNumber, final int LFS) {
		
		if((packetNumber * dataSize) >= bytes.length) {
			return null;
		}
		
		final int lastPacket = ((((packetNumber+1) * dataSize) >= bytes.length) ? 1 : 0);
		
		final byte[] dataBytes = makeBody(packetNumber);
		final byte[] headerBytes = makeHeader(LFS, lastPacket);
		
		
		byte[] packetBytes = new byte[dataBytes.length + headerBytes.length];
		System.arraycopy(headerBytes, 0, packetBytes, 0, headerBytes.length);
		System.arraycopy(dataBytes, 0, packetBytes, headerBytes.length, dataBytes.length);
		
		DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
		
		return Optional.of(packet);
	}
	
	public byte[] makeBody(int packetNumber) {
		int to = Math.min(packetNumber * dataSize + dataSize, bytes.length);
		return Arrays.copyOfRange(bytes, packetNumber * dataSize, to);
	}
	
	public byte[] makeHeader(int LFS, int lastPacket) {
		return new byte[] { (byte) LFS, (byte) lastPacket };
	}

}
