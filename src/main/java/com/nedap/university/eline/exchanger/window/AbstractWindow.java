package com.nedap.university.eline.exchanger.window;

public abstract class AbstractWindow {

    public static final int SENDING_WINDOW_SIZE = 750; //TODO make it possible to change this depending on time out / DACK occurence
    public static final int SEQUENCE_NUMBER_SPACE = 2000;
    public static final int RECEIVING_WINDOW_SIZE = 750;
	
	public static boolean isInWindow(final int oneBeforeStartWindow, final int aNumber, final String windowType) {
		
		if (windowType != "SWS" && windowType != "RWS") {
			throw new IllegalArgumentException();
		}
		
		int windowSize = setWindowSize(windowType);
		if (oneBeforeStartWindow == -1) {
			return isInWindowWhenNoPacketReceivedYet(aNumber, windowSize);
		}
		
		if (oneBeforeStartWindow < 0 || oneBeforeStartWindow >= SEQUENCE_NUMBER_SPACE) {
			throw new IllegalArgumentException();
		}
		
		final boolean upperBoundFallsOutOfRange = ((windowSize + oneBeforeStartWindow) / SEQUENCE_NUMBER_SPACE) != 0;
		if(!upperBoundFallsOutOfRange) {
			return isInWindowWithContinuousRange(oneBeforeStartWindow, aNumber, windowSize);
		} else {
			return isInWindowWithRangeInTwoParts(oneBeforeStartWindow, aNumber, windowSize);
		}
	}
	
	private static boolean isInWindowWhenNoPacketReceivedYet(final int aNumber, final int windowSize) {
		return (aNumber >= 0 && aNumber < windowSize);
	}
	
	public static int setWindowSize(final String windowType) {
		if (windowType == "SWS") {
			return SENDING_WINDOW_SIZE;
		} else {
			return RECEIVING_WINDOW_SIZE;
		}
	}
	
	public static boolean isInWindowWithContinuousRange(final int oneBeforeStartWindow, final int aNumber, final int windowSize) {
		final int lowerbound = oneBeforeStartWindow;
		final int upperbound = windowSize + oneBeforeStartWindow;
		if (aNumber > lowerbound && aNumber <= upperbound) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isInWindowWithRangeInTwoParts(final int oneBeforeStartWindow, final int aNumber, final int windowSize) {
		final int lowerboundFirstPart = oneBeforeStartWindow;
		final int upperboundFirstPart = SEQUENCE_NUMBER_SPACE - 1;
		
		final int lowerboundSecondPart = -1;
		final int upperboundSecondPart = (windowSize + oneBeforeStartWindow) % SEQUENCE_NUMBER_SPACE;
		
		return ((aNumber > lowerboundFirstPart && aNumber <= upperboundFirstPart) ||
				aNumber > lowerboundSecondPart && aNumber <= upperboundSecondPart);
	}
}
