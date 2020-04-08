package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SendingWindowTransmitter implements TimeoutEventHandlerInterface {
    
	private SendingWindow sws;
    private byte[] bytesToBeSend;
    private int numberOfPackets;
    private int lastPacket = 0;
	private boolean lastPacketSent = false;
	private Timeout timeout;
	
	//TODO pull this apart?
    private InetAddress destAddress;
    private int destPort;
    private Map<Integer, DatagramPacket> sentNotAckedPackets = new HashMap<>();
    private DatagramSocket socket;
	
	
    public SendingWindowTransmitter(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
		this.sws= new SendingWindow();
		this.bytesToBeSend = bytes;
		this.numberOfPackets = (int) Math.ceil(bytesToBeSend.length / sws.getDataSize());
		this.timeout = new Timeout();
    	this.destAddress = destAddress;
    	this.destPort = destPort;
    	this.socket = socket;
    	
    }
    
	public void uploadFile() {
		
		new Thread(() -> sendPackets()).start();
		
		new Thread(() -> checkAcks()).start();
		
		//TODO do some checking here?
    }
	
	public void sendPackets() {
		//TODO add file name
		System.out.println("Sending a file, total number of packets to send is " + numberOfPackets);
		
		while(!lastPacketSent) {
			
			sendPacket();
			//TODO: add timeout
		}
	}
	
	public void sendPacket() {
		boolean packetSent = false;
		try {
			synchronized(sws) {
				if(sws.getSubsequentLFS() == (sws.getK()-1)) {
					//TODO add name of file, maybe change this to some other way to track status
					System.out.println("Still working on sending the file! Sent 256 packets since last message");
				}
				
				if(sws.isInWindow(sws.getLAR(), sws.getSubsequentLFS(), "SWS")) {
					final DatagramPacket packet = makePacket();
					socket.send(packet);
			    	Timeout.SetTimeout(2000, this, sws.getPacketNumber());
					sentNotAckedPackets.put(sws.getLFS(), packet);
					if(sws.getPacketNumber() == numberOfPackets) {
						lastPacketSent = true;
					}
					packetSent = true;
				} 
			}
    	} catch (IOException e) {
    		System.out.println("Packet could not be sent, error message: " + e.getMessage());
		} 
		
		if (!packetSent) {
			try {
				//TODO there should be a way to do this without a sleep
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Transmitter, sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
			} 
		}
	}

	
	public DatagramPacket makePacket() {
		sws.incrementLFS();
		sws.incrementPacketNumber();
		
		lastPacket = ((((sws.getPacketNumber()+1) * sws.getDataSize()) >= bytesToBeSend.length) ? 1 : 0);
		
		byte[] headerBytes = { (byte) sws.getLFS(), (byte) lastPacket };
		
		int to = Math.min((sws.getPacketNumber()) * sws.getDataSize() + sws.getDataSize(), bytesToBeSend.length);
		
		byte[] dataBytes = Arrays.copyOfRange(bytesToBeSend, sws.getPacketNumber() * sws.getDataSize(), to);
		byte[] packetBytes = new byte[dataBytes.length + headerBytes.length];
		System.arraycopy(headerBytes, 0, packetBytes, 0, sws.getHeaderSize());
		System.arraycopy(dataBytes, 0, packetBytes, sws.getHeaderSize(), dataBytes.length);

		return new DatagramPacket(packetBytes, packetBytes.length, destAddress, destPort);
	}
    
    public void checkAcks() {
    	boolean lastAck = false;
    	while(!lastAck) {
	        try {
	        	byte[] buffer = new byte[2];
	        	DatagramPacket response = new DatagramPacket(buffer, buffer.length);
				socket.receive(response);
				synchronized(sws) {
					lastAck = ((response.getData()[0] &0xFF) == 1) ? true : false;
					final int seqNumber = response.getData()[1] &0xFF;
					
					if (seqNumber == (sws.getLAR())) {
						sws.incrementDACKs();
						if (sws.getDACKs() == 3) {
							socket.send(sentNotAckedPackets.get(seqNumber + 1));
						}
					} else {
						sws.setDACKsToZero();
						sws.setLAR(seqNumber);
						sentNotAckedPackets.remove(seqNumber);
					} 
					sws.setAckRec(true);
				}
			} catch (IOException e) {
				System.out.println("Acks could not be received or a packet could not be retransmitted, error message: " + e.getMessage());
			}
	    }
    	System.out.println("All done!");
    }

	@Override
	public void TimeoutElapsed(Object tag) {
        int packetNumber =(Integer)tag;
        if(sentNotAckedPackets.containsKey(packetNumber%sws.getK())) {
        	System.out.println("Timer expired for packet with the number " + packetNumber + ". Resending packet.");
        	try {
				socket.send(sentNotAckedPackets.get(packetNumber%sws.getK()));
			} catch (IOException e) {
				System.out.println("Packet with the number " + packetNumber + " could not be retransmitted after a timeout"
						+ ", error message: " + e.getMessage());
			}
        }
	}
}
