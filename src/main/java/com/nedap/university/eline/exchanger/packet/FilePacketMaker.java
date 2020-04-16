package com.nedap.university.eline.exchanger.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.manager.FileSendManager.sendReason;
import com.nedap.university.eline.exchanger.window.AbstractWindow;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class FilePacketMaker {
	
	private SendingWindow sendingWindow;
	private FilePacketSender filePacketSender;
	
	private byte[] bytes;
	private InetAddress destAddress;
    private int destPort;
    public enum CanSend { YES, NO_MORE_PACKETS, NOT_IN_WINDOW };
	
	public FilePacketMaker(final byte[] bytesToBeSend, final InetAddress destAddress, final int destPort, final SendingWindow sendingWindow, FilePacketSender filePacketSender) {
		this.bytes = bytesToBeSend;
		this.destAddress = destAddress;
    	this.destPort = destPort;
    	this.sendingWindow = sendingWindow;
    	this.filePacketSender = filePacketSender;
	}
	
	public CanSend sendNextPacketIfPossible() {
		synchronized(sendingWindow) {
			CanSend canSend = canSendNextPacket();
			if (canSend == CanSend.YES) {
				makeAndSendPacket();
			}
			return canSend;
		}
	}
	
	private CanSend canSendNextPacket() {
		if(((sendingWindow.getPacketNumber() + 1) * FilePacketContents.DATASIZE) >= bytes.length) {
			return CanSend.NO_MORE_PACKETS;
		} else if(!sendingWindow.isComingSeqNumInWindow()) {
			return CanSend.NOT_IN_WINDOW;
		} else {
			return CanSend.YES;
		}
	}
	
	private void makeAndSendPacket() {
		DatagramPacket packet = makeDataPacket();
		sendFilePacket(packet, sendReason.PRIMARY);
	}
	
	private DatagramPacket makeDataPacket() {
		sendingWindow.incrementPacketNumber();
		
		final int packetNumber = sendingWindow.getPacketNumber();
		
		if(!sendingWindow.isInWindow(packetNumber%AbstractWindow.SEQUENCE_NUMBER_SPACE)) {
			throw new IllegalArgumentException();
		}
		
		final int lastPacket = ((((packetNumber+1) * FilePacketContents.DATASIZE) >= bytes.length) ? 1 : 0);
		final byte[] packetBytes = makeByteArrayForPacket(packetNumber, packetNumber%AbstractWindow.SEQUENCE_NUMBER_SPACE, lastPacket);
		
		return new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
	}
	
	private byte[] makeByteArrayForPacket(final int packetNumber, final int seqNum, final int lastPacket) {
		byte[] header = makeHeaderWithoutChecksum(seqNum, lastPacket);
		byte[] body = makeBody(packetNumber);
		
		byte[] packetBytesWithoutChecksum = new byte[header.length + body.length];
		System.arraycopy(header, 0, packetBytesWithoutChecksum, 0, header.length);
		System.arraycopy(body, 0, packetBytesWithoutChecksum, header.length, body.length);
		
		return ChecksumGenerator.addChecksumToPacketByte(packetBytesWithoutChecksum);
	}

	private byte[] makeHeaderWithoutChecksum(final int seqNumber, final int lastPacket) {
		byte[] seqNumInBytes = SequenceNumberCalculator.turnSeqNumIntoBytes(seqNumber);
		
		byte[] headerBytes = new byte[FilePacketContents.HEADERSIZE - ChecksumGenerator.CHECKSUM_LENGTH];
		System.arraycopy(seqNumInBytes, 0, headerBytes, 0, SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH);
		headerBytes[SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH] = (byte) lastPacket;
		
		return headerBytes;
	}
	
	private byte[] makeBody(final int packetNumber) {
		int to = Math.min(packetNumber * FilePacketContents.DATASIZE + FilePacketContents.DATASIZE, bytes.length);
		return Arrays.copyOfRange(bytes, packetNumber * FilePacketContents.DATASIZE, to);
	}	
	
	private void sendFilePacket(final DatagramPacket packet, final sendReason reason) {
		synchronized(sendingWindow) {
			final int seqNum = sendingWindow.getSeqNumOneGreaterThanLastSent();
			filePacketSender.sendFilePacket(packet, reason, seqNum);
			sendingWindow.incrementLastFrameSent();
		}
    }
	
	public int getNumberRetransmissions() {
		return filePacketSender.getNumberRetransmissions();
	}
}
