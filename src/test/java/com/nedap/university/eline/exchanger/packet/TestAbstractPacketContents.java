package com.nedap.university.eline.exchanger.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.UnknownHostException;

import org.junit.Test;

public class TestAbstractPacketContents {
	@Test
	public void testGetBooleanFromByte() throws UnknownHostException {
		assertEquals(true, AbstractPacketContents.getBooleanFromInt(1));
		assertEquals(false, AbstractPacketContents.getBooleanFromInt(0));
		assertThrows(IllegalArgumentException.class, () -> AbstractPacketContents.getBooleanFromInt(2));
	}
}
