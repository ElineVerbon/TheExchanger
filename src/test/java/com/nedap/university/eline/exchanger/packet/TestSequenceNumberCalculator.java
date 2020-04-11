package com.nedap.university.eline.exchanger.packet;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class TestSequenceNumberCalculator {
	
	@Test
	public void testGetSeqNumFromBytes() {
		final Map<Integer, byte[]> testCases = new HashMap<>();
		testCases.put(0, new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 });
		testCases.put(476, new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 });
		testCases.put(538593, new byte[] { (byte) 0b00000000, (byte) 0b00001000, (byte) 0b00110111, (byte) 0b11100001 });
		testCases.put((int) (Math.pow(2, 32)-1), new byte[] { (byte) 0b01111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 });
		
		for (Map.Entry<Integer, byte[]> entry : testCases.entrySet()) {
			assertEquals(entry.getKey(), SequenceNumberCalculator.getSeqNumFromBytes(entry.getValue()));
		}
	}
	
	@Test
	public void testTurnSeqNumIntoBytes() {
		final Map<Integer, byte[]> testCases = new HashMap<>();
		testCases.put(0, new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000 });
		testCases.put(476, new byte[] { (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000001, (byte) 0b11011100 });
		testCases.put(538593, new byte[] { (byte) 0b00000000, (byte) 0b00001000, (byte) 0b00110111, (byte) 0b11100001 });
		testCases.put((int) (Math.pow(2, 32) - 1), new byte[] { (byte) 0b01111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 });
		
		for (Map.Entry<Integer, byte[]> entry : testCases.entrySet()) {
			assertArrayEquals(entry.getValue(), SequenceNumberCalculator.turnSeqNumIntoBytes(entry.getKey()));
		}
	}
	
	@Test 
	public void testTurnSeqNumIntoBytesWithWrongSeqNum() {
		assertThrows(IllegalArgumentException.class, () -> SequenceNumberCalculator.turnSeqNumIntoBytes(-1));
	}
	
	@Test 
	public void testGetSeqNumFromBytesWithWrongBytes() {
		assertThrows(IllegalArgumentException.class, 
				() -> SequenceNumberCalculator.getSeqNumFromBytes( new byte[] { (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111 }));
	}

}
