package com.nedap.university.eline.exchanger.packet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class TestFilePacketContents {
	@Test
	public void testFalsePacket() throws UnknownHostException {
		DatagramPacket packet = new DatagramPacket(new byte[] { (byte) 0b00000000, (byte) 0b00000000 }, 0);
		assertThrows(IllegalArgumentException.class, () -> new FilePacketContents(packet));
	}

	@Test
	public void testMakePacketAndGetContentInfo() throws UnknownHostException {
		byte[] bytes = new byte[] { (byte) 0b00000000, (byte) 0b00000000 };
		FilePacketMaker maker = new FilePacketMaker(bytes, InetAddress.getLocalHost(), 8080);
		DatagramPacket packet = maker.makeDataPacket(0, 0).get();
		
		FilePacketContents contents = new FilePacketContents(packet);
		
		assertArrayEquals(bytes, contents.getDataBytes());
		final byte[] allBytes = new byte[FilePacketContents.HEADERSIZE + bytes.length];
		System.arraycopy(SequenceNumberCalculator.turnSeqNumIntoBytes(0), 0, allBytes, 1, SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH);
		allBytes[SequenceNumberCalculator.SEQ_NUM_BYTE_LENGTH] = (byte) 1;
		System.arraycopy(bytes, 0, allBytes, FilePacketContents.HEADERSIZE, bytes.length);
		assertArrayEquals(allBytes, contents.getBytes());
		assertEquals(0, contents.getSeqNum());
		assertEquals(true, contents.isLastPacket());
	}
}
