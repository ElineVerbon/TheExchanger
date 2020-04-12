package com.nedap.university.eline.exchanger.window;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAbstractWindow {
	
	@Test
	public void testInvalidWindowType() {
		assertThrows(IllegalArgumentException.class, 
				() -> AbstractWindow.isInWindow(0, AbstractWindow.SENDING_WINDOW_SIZE, "VWS"));
	}
	
	@Test
	public void testIsInWindowWhenNoPacketReceivedYet() {
		assertEquals(true,
				AbstractWindow.isInWindow(-1, 0, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(-1, -1, "SWS"));
		assertEquals(true,
				AbstractWindow.isInWindow(-1, AbstractWindow.SENDING_WINDOW_SIZE-1, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(-1, AbstractWindow.SENDING_WINDOW_SIZE, "SWS"));
		assertEquals(true,
				AbstractWindow.isInWindow(-1, AbstractWindow.SENDING_WINDOW_SIZE-1, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(-1, AbstractWindow.SENDING_WINDOW_SIZE, "RWS"));
	}
	
	@Test
	public void testInvalidStartWindowNumber() {
		assertThrows(IllegalArgumentException.class, 
				() -> AbstractWindow.isInWindow(-2, AbstractWindow.SENDING_WINDOW_SIZE, "SWS"));
		assertThrows(IllegalArgumentException.class, 
				() -> AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE, AbstractWindow.SENDING_WINDOW_SIZE, "SWS"));
	}
	
	@Test
	public void testIsInContinuousSendingWindow() {
		assertEquals(true,
				AbstractWindow.isInWindow(0, AbstractWindow.SENDING_WINDOW_SIZE, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(0, AbstractWindow.SENDING_WINDOW_SIZE + 1, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(0, 0, "SWS"));
	}
	
	@Test
	public void testInSendingWindowWithRangeInTwoParts() {
		assertEquals(true,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE - 1, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE, "SWS"));
		
		assertEquals(true,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, 0, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, -1, "SWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SENDING_WINDOW_SIZE, "SWS"));
	}
	
	@Test
	public void testIsInContinuousReceivingWindow() {
		assertEquals(true,
				AbstractWindow.isInWindow(0, AbstractWindow.RECEIVING_WINDOW_SIZE, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(0, AbstractWindow.RECEIVING_WINDOW_SIZE + 1, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(0, 0, "RWS"));
	}
	
	@Test
	public void testInReceivingWindowWithRangeInTwoParts() {
		assertEquals(true,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE - 1, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.SEQUENCE_NUMBER_SPACE, "RWS"));
		
		assertEquals(true,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, 0, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, -1, "RWS"));
		assertEquals(false,
				AbstractWindow.isInWindow(AbstractWindow.SEQUENCE_NUMBER_SPACE - 2, AbstractWindow.RECEIVING_WINDOW_SIZE, "RWS"));
	}

}
