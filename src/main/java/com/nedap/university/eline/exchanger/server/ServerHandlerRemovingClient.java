package com.nedap.university.eline.exchanger.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class ServerHandlerRemovingClient {

	public void letUserRemoveFile(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			File file = new File("/home/pi/" + fileName);
			boolean success = file.delete();
			int intSuccess = (success) ? 1 : 0;
			new DatagramPacket( new byte[] { (byte) intSuccess }, 1, clientAddress, clientPort);
			thisCommunicationsSocket.send(new DatagramPacket( new byte[] { (byte) intSuccess }, 1, clientAddress, clientPort));
			
			thisCommunicationsSocket.close();
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
