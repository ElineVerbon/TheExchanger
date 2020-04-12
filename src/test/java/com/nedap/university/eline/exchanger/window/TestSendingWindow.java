package com.nedap.university.eline.exchanger.window;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;

public class TestSendingWindow {
	
	@Test
	public void testMethodsLastFrameSent() {
		SendingWindow window = new SendingWindow();
		assertEquals(-1, window.getLastFrameSent());
		assertEquals(0, window.getSeqNumOneGreaterThanLastSent());
		window.incrementLastFrameSent();
		assertEquals(0, window.getLastFrameSent());
		for(int i = 1; i < SendingWindow.SEQUENCE_NUMBER_SPACE; i++) {
			assertEquals(i, window.getSeqNumOneGreaterThanLastSent());
			window.incrementLastFrameSent();
		}
		assertEquals(SendingWindow.SEQUENCE_NUMBER_SPACE - 1, window.getLastFrameSent());
		assertEquals(0, window.getSeqNumOneGreaterThanLastSent());
		window.incrementLastFrameSent();
		assertEquals(0, window.getLastFrameSent());
	}
	
	@Test
	public void testMethodsLastAckknowledgementReceived() {
		SendingWindow window = new SendingWindow();
		assertThrows(IllegalArgumentException.class, () -> window.setLastAckknowledgementReceived(-1));
		assertThrows(IllegalArgumentException.class, () -> window.setLastAckknowledgementReceived(SendingWindow.SEQUENCE_NUMBER_SPACE));
		window.setLastAckknowledgementReceived(0);
		assertEquals(0, window.getLastAckknowledgementReceived());
		window.setLastAckknowledgementReceived(SendingWindow.SEQUENCE_NUMBER_SPACE - 1);
		assertEquals(SendingWindow.SEQUENCE_NUMBER_SPACE - 1, window.getLastAckknowledgementReceived());
	}
	
	@Test
	public void testMethodsDuplicateAcks() {
		SendingWindow window = new SendingWindow();
		assertEquals(0, window.getDuplicateACKs());
		window.incrementDuplicateACKs();
		assertEquals(1, window.getDuplicateACKs());
		window.setDuplicateACKsToZero();
		assertEquals(0, window.getDuplicateACKs());
	}
	
	@Test
	public void testMethodsPacketNumber() {
		SendingWindow window = new SendingWindow();
		assertEquals(-1, window.getPacketNumber());
		window.incrementPacketNumber();
		assertEquals(0, window.getPacketNumber());
		window.incrementPacketNumber();
		assertEquals(1, window.getPacketNumber());
	}
	
	@Test
	public void testIsInWindow() {
		SendingWindow window = new SendingWindow();
		assertEquals(true, window.isInWindow(0));
		assertEquals(false, window.isInWindow(-1));
		assertEquals(true, window.isInWindow(window.getSendingWindowSize()-1));
		assertEquals(false, window.isInWindow(window.getSendingWindowSize()));
		
		for(int i = -1; i < SendingWindow.SEQUENCE_NUMBER_SPACE-2; i++) {
			window.incrementLastFrameSent();
		}
		assertEquals(false, window.isInWindow(SendingWindow.SEQUENCE_NUMBER_SPACE-1));
		assertEquals(false, window.isInWindow(SendingWindow.SEQUENCE_NUMBER_SPACE-2));
		assertEquals(true, window.isInWindow(0));
		assertEquals(true, window.isInWindow(window.getSendingWindowSize()-2));
	}

}
