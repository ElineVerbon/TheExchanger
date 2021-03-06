package com.nedap.university.eline.exchanger.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;

public class ChoiceCommunicator {
	private int generalServerPort;
	private InetAddress serverAddress;
	
	public ChoiceCommunicator(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
	public int getGeneralServerPort() {
		return generalServerPort;
	}
	
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	public DatagramPacket communicateChoiceToServerAndExpectLongerResponse(final byte[] choiceByte, final byte[] dataBytes, 
			final DatagramSocket socket) throws SocketTimeoutException {
    	DatagramPacket packet = makeDataPacket(choiceByte, dataBytes);
    	sendToServer(packet, socket);
    	DatagramPacket response = receivePacket(socket, 2000);
    	return response;
    }
	
	public DatagramPacket communicateChoiceToServerWithChecksum(final byte[] choiceByte, final byte[] dataBytes,
			final byte[] checksumBytes, final DatagramSocket socket) throws SocketTimeoutException {
    	DatagramPacket packet = makeDataPacketWithCheckSum(checksumBytes, choiceByte, dataBytes);
    	sendToServer(packet, socket);
    	DatagramPacket response = receivePacket(socket, 1);
    	return response;
    }
	
	public DatagramPacket communicateChoiceToServer(final byte[] choiceByte, final byte[] dataBytes, 
			final DatagramSocket socket) throws SocketTimeoutException {
    	DatagramPacket packet = makeDataPacket(choiceByte, dataBytes);
    	sendToServer(packet, socket);
    	DatagramPacket response = receivePacket(socket, 1);
    	return response;
    }
    
    private DatagramPacket makeDataPacket(final byte[] choiceByte, final byte[] dataBytes) {
		byte[] packetBytes = new byte[dataBytes.length + choiceByte.length];
		System.arraycopy(choiceByte, 0, packetBytes, 0, choiceByte.length);
		System.arraycopy(dataBytes, 0, packetBytes, choiceByte.length, dataBytes.length);
		return new DatagramPacket(packetBytes, packetBytes.length, serverAddress, generalServerPort);
	}
    
    private DatagramPacket makeDataPacketWithCheckSum(final byte[] checksum, final byte[] choiceByte, final byte[] dataBytes) {
    	byte[] packetBytes = new byte[checksum.length + dataBytes.length + choiceByte.length];
		System.arraycopy(choiceByte, 0, packetBytes, 0, choiceByte.length);
		System.arraycopy(checksum, 0, packetBytes, 1, checksum.length);
		System.arraycopy(dataBytes, 0, packetBytes, checksum.length + choiceByte.length, dataBytes.length);
		
		return new DatagramPacket(packetBytes, packetBytes.length, serverAddress, generalServerPort);
    }
    
    private void sendToServer(DatagramPacket packet, DatagramSocket socket) {
    	try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send the choice to the server.");
		}
    }
    
    private DatagramPacket receivePacket(DatagramSocket socket, final int bufferSize) throws SocketTimeoutException {
		DatagramPacket response = null;
    	try {
    		response = new DatagramPacket(new byte[bufferSize], bufferSize);
			socket.setSoTimeout(5000);
			socket.receive(response);
		} catch (SocketTimeoutException e) {
			throw new SocketTimeoutException();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return response;
    }
}
