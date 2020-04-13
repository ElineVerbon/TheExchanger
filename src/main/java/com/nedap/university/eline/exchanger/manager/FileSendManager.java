package com.nedap.university.eline.exchanger.manager;

import java.net.DatagramSocket;
import java.net.InetAddress;

import com.nedap.university.eline.exchanger.executor.AckReceiver;
import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.SentFilePacketTracker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker.CanSend;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class FileSendManager {
    
	private FilePacketMaker filePacketMaker;
	private AckReceiver ackReceiver;
	
	public enum sendReason { PRIMARY, DACK, TIMER }
	private boolean noMorePackets = false;
	private boolean lastAck = false;
	
    public FileSendManager(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket) {
    	SendingWindow sendingWindow = new SendingWindow();
    	SentFilePacketTracker packetTracker = new SentFilePacketTracker();
    	FilePacketSender filePacketSender = new FilePacketSender(socket, packetTracker, sendingWindow);
		this.filePacketMaker = new FilePacketMaker(bytes, destAddress, destPort, sendingWindow, filePacketSender);
		this.ackReceiver = new AckReceiver(socket, packetTracker, sendingWindow, filePacketSender);
    }
    
	public void sendFile() {
		
		new Thread(() -> sendPackets()).start();
		
		new Thread(() -> checkAcks()).start();
    }
	
	public void sendPackets() {
		while(!noMorePackets) {
			CanSend result = filePacketMaker.canSendNextPacket();
			if (result == CanSend.NOT_IN_WINDOW) {
				waitABit();
			} else if (result == CanSend.NO_MORE_PACKETS) {
				noMorePackets = true;
			} else if (result == CanSend.YES) {
				filePacketMaker.makeAndSendPacket();
			}
		}
	}
	
	public void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println("Transmitter at sendPackets() thread was interrupted while sleeping. Error message: " + e.getMessage());
		}
	}
	
    public void checkAcks() {
    	while (!lastAck) {
    		lastAck = ackReceiver.receiveAndProcessAck();
	    } 
    	System.out.println("File was successfully uploaded!");
    }
}
