package com.nedap.university.eline.exchanger.manager;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import com.nedap.university.eline.exchanger.client.TransferStatistics;
import com.nedap.university.eline.exchanger.executor.AckReceiver;
import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.SentFilePacketTracker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker;
import com.nedap.university.eline.exchanger.packet.FilePacketMaker.CanSend;
import com.nedap.university.eline.exchanger.window.SendingWindow;

public class FileSendManager implements Runnable {
    
	private FilePacketMaker filePacketMaker;
	private AckReceiver ackReceiver;
	private String fileName;
	private DatagramSocket socket;
	
	private String result;
	private long startTime;
	private long stopTime;
	private String transferType;
	private boolean done;
	private int fileSize;
	
	public enum sendReason { PRIMARY, DACK, TIMER }
	private boolean noMorePackets = false;
	private boolean lastAck = false;
	private volatile boolean flag = true;
	private Thread ackThread;
	
    public FileSendManager(byte[] bytes, final InetAddress destAddress, final int destPort, final DatagramSocket socket, 
    		final String fileName, final String transferType) {
    	this.socket = socket;
    	SendingWindow sendingWindow = new SendingWindow();
    	SentFilePacketTracker packetTracker = new SentFilePacketTracker();
    	FilePacketSender filePacketSender = new FilePacketSender(socket, packetTracker, sendingWindow);
		this.filePacketMaker = new FilePacketMaker(bytes, destAddress, destPort, sendingWindow, filePacketSender);
		this.ackReceiver = new AckReceiver(socket, packetTracker, sendingWindow, filePacketSender);
		this.fileName = fileName;
		this.transferType = transferType;
		this.fileSize = bytes.length;
    }
    
	public void run() {
		System.out.println("File " + fileName + " is being uploaded.");
		
		startTime = System.nanoTime();
		
		ackThread = new Thread(() -> checkAcks());
		ackThread.start();
		
		while(flag && !noMorePackets) {
			if (Thread.interrupted()) {
				handlePacketThreadInterruption();
			}
			CanSend result = filePacketMaker.sendNextPacketIfPossible();
			if (result == CanSend.NOT_IN_WINDOW) {
				waitABit();
			} else if (result == CanSend.NO_MORE_PACKETS) {
				noMorePackets = true;
			}  
		}
    }
	
	public void stopRunning() {
		flag = false;
	}
	
	private void waitABit() {
		try {
			//TODO there should be a way to do this without a sleep
			Thread.sleep(500);
		} catch (InterruptedException e) {
			handlePacketThreadInterruption();
		}
	}
	
    public void checkAcks() {
    	
    	while (!lastAck) {
    		try {
				lastAck = ackReceiver.receiveAndProcessAck();
			} catch (SocketTimeoutException e) {
				if(Thread.interrupted()) {
	    			handleAckThreadInterruption();
	    		} else {
					System.out.println("> Sending the file " + fileName + " failed because the socket connection broke down.");
					return;
	    		}
			}
	    } 
    	socket.close();
    	
    	done = true;
    	stopTime = System.nanoTime();
    	System.out.println("> File " + fileName + " was successfully uploaded!");
    }
    
    private void handlePacketThreadInterruption() {
    	ackThread.interrupt();
    	try {
			while (true) {
				Thread.sleep(60*60*1000);
			}
		} catch (InterruptedException e2) {
			ackThread.interrupt();
		}
    }
    
    private void handleAckThreadInterruption() {
		try {
			while (true) {
				Thread.sleep(60*60*1000);
			}
		} catch (InterruptedException e2) {
		}
    }
    
    public boolean isDone() {
    	return done;
    }
    
    public String getStatistics() {
    	if (!done) {
    		return "Still working on this transfer!";
    	}
    	String elapsedTime = TransferStatistics.getElapsedTime(startTime, stopTime);
    	int numberRetransmissions = filePacketMaker.getNumberRetransmissions();
		String result = 
				String.format("%-40s %-20s %-15s %-20s %-20s\n", fileName, "| " + fileSize, "| " + transferType, "| " + elapsedTime, "| " + numberRetransmissions);
		
		return result;
    }
}
