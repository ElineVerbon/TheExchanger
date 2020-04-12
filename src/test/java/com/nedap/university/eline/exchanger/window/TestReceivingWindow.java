package com.nedap.university.eline.exchanger.window;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestReceivingWindow {

	@Test
	public void testIncrementAndGetLargestConsecutivePacketReceived() {
		
		ReceivingWindow window = new ReceivingWindow();
		window.setLargestConsecutivePacketReceived(AbstractWindow.SEQUENCE_NUMBER_SPACE-1);
		window.incrementLargestConsecutivePacketReceived();
		assertEquals(0, window.getLargestConsecutivePacketReceived());
		
		window.setLargestConsecutivePacketReceived(10);
		window.incrementLargestConsecutivePacketReceived();
		assertEquals(11, window.getLargestConsecutivePacketReceived());
		
		assertThrows(IllegalArgumentException.class, () -> window.setLargestConsecutivePacketReceived(-1));
		assertThrows(IllegalArgumentException.class, () -> window.setLargestConsecutivePacketReceived(ReceivingWindow.SEQUENCE_NUMBER_SPACE));
	}
	
	@Test
	public void testGetSubsequentLargestConsecutivePacketReceived() {
		ReceivingWindow window = new ReceivingWindow();
		assertEquals(0, window.getSubsequentLargestConsecutivePacketReceived());
		
		window.setLargestConsecutivePacketReceived(AbstractWindow.SEQUENCE_NUMBER_SPACE-1);
		assertEquals(0, window.getSubsequentLargestConsecutivePacketReceived());
		
		window.setLargestConsecutivePacketReceived(210);
		assertEquals(211, window.getSubsequentLargestConsecutivePacketReceived());
	}
	
	@Test
	public void testIsInWindow() {
		ReceivingWindow window = new ReceivingWindow();
		assertEquals(true, window.isInWindow(5));
		assertEquals(false, window.isInWindow(-1));
		assertEquals(true, window.isInWindow(window.getReceivingWindowSize()-1));
		assertEquals(false, window.isInWindow(window.getReceivingWindowSize()));
		
		window.setLargestConsecutivePacketReceived(AbstractWindow.SEQUENCE_NUMBER_SPACE-5);
		assertEquals(true, window.isInWindow(window.getReceivingWindowSize()-1-5));
		assertEquals(false, window.isInWindow(window.getReceivingWindowSize()-1));
		assertEquals(false, window.isInWindow(-1));
		assertEquals(false, window.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE));
	}
}
