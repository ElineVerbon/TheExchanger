package com.nedap.university.eline.exchanger.packet;

public class AbstractPacketContents {
	
	public static boolean getBooleanFromInt(final int i) {
		if (i == 0) {
			return false;
		} else if (i == 1) {
			return true;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
