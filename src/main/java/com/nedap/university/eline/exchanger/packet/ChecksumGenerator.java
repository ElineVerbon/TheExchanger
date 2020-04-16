package com.nedap.university.eline.exchanger.packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
	
	public static byte[] addChecksumToPacketByte(final byte[] bytes) {
		byte[] checksumBytes = ChecksumGenerator.getCheckSum(bytes);
		byte[] packetBytes = new byte[ChecksumGenerator.CHECKSUM_LENGTH + bytes.length];
		System.arraycopy(checksumBytes, 0, packetBytes, 0, checksumBytes.length);
		System.arraycopy(bytes, 0, packetBytes, ChecksumGenerator.CHECKSUM_LENGTH, bytes.length);
		
		return packetBytes;
	}
	
	public static boolean isPacketIntact(final FilePacketContents packet) {
		final byte[] givenChecksum = packet.getChecksum();
		final byte[] bytes = packet.getBytes();
		final byte[] bytesWithoutChecksum = Arrays.copyOfRange(bytes, ChecksumGenerator.CHECKSUM_LENGTH, bytes.length);
		final byte[] calculatedChecksum = ChecksumGenerator.getCheckSum(bytesWithoutChecksum);
		return (Arrays.equals(givenChecksum, calculatedChecksum));
	}
	
	public static byte[] createChecksumFromFile(File filename) throws Exception {
		InputStream fis =  new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
               complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}
}
