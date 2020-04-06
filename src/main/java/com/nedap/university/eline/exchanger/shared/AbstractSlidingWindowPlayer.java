package com.nedap.university.eline.exchanger.shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

abstract class AbstractSlidingWindowPlayer {

	static final int HEADERSIZE = 2;
	static final int DATASIZE = 512;
    static final int SWS = 50; //TODO make it possible to change this depending on time out / DACK occurence
    static final int K = 256;
    static final int RWS = 50;
    
    public int seqNumToAckNum(final int seqNumber, final int lastAck, final int lastSeqNumber) {
    	final int seqRound = (seqNumber >= lastSeqNumber) ? lastAck / K : lastAck / K + 1;
    	return seqNumber + (seqRound * K);
    }
    
    public void sendPacket(byte[] bytes, InetAddress address, int port, DatagramSocket socket) {
    	try {
			socket.send(new DatagramPacket(bytes, bytes.length, address, port));
		} catch (IOException e) {
			System.out.println("Could not send packet to " + address + ". Error message : " + e.getMessage());
		}
    }
}
