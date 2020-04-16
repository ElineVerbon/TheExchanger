package com.nedap.university.eline.exchanger.packet;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumGenerator {

	public static final int CHECKSUM_LENGTH = 16;
	
	public static byte[] getCheckSum(final byte[] dataBytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] theChecksum = md.digest(dataBytes);
			return theChecksum;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
