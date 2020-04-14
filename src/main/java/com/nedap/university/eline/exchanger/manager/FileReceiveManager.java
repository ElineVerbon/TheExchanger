package com.nedap.university.eline.exchanger.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import com.nedap.university.eline.exchanger.executor.AckSender;
import com.nedap.university.eline.exchanger.executor.ReceivedFilePacketTracker;
import com.nedap.university.eline.exchanger.executor.Receiver;
import com.nedap.university.eline.exchanger.packet.FilePacketContents;
import com.nedap.university.eline.exchanger.packet.AckPacketMaker;
import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class FileReceiveManager {
	
	private ReceivingWindow receivingWindow;
	private ReceivedFilePacketTracker packetTracker;
	private Receiver receiver;
	private AckPacketMaker ackMaker;
	private AckSender ackSender;
	
	private int[] lastAckedSeqNumPacNumPair = new int[] { -1, -1 };
	public boolean recAllPackets = false;
	private boolean recLastPacket = false;
	private boolean duplicateAck = false;
	
	private String absoluteFilePath;
	private String fileName;
	private DatagramSocket socket;
	
	public FileReceiveManager(final DatagramSocket socket, final InetAddress sourceAddress, final int sourcePort, final String absoluteFilePath, final String fileName) {
		this.socket = socket;
		this.receivingWindow= new ReceivingWindow();
		this.packetTracker = new ReceivedFilePacketTracker();
		receiver = new Receiver(socket);
		this.ackMaker = new AckPacketMaker(sourceAddress, sourcePort);
		this.ackSender = new AckSender(socket);
		
		this.absoluteFilePath = absoluteFilePath;
		this.fileName = fileName;
    }
	
	public void receiveFile() {
		System.out.println("Receiving the file " + fileName + ".");
		
		receivePackets();
	}
	
	public void receivePackets() {
		while(!recAllPackets) {
			final DatagramPacket packet = receiver.receivePacket(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE);
			processPacket(new FilePacketContents(packet));
		}
		
		waitToVerifySenderHasReceivedAckAndIfNotSendAgain();
		
		saveFile();
		socket.close();
	}
	
	public void waitToVerifySenderHasReceivedAckAndIfNotSendAgain() {
		final DatagramPacket possiblePacket = receiver.receivePacketWithTimeOut(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE);
		if(possiblePacket != null) {
			sendDuplicateAck();
			waitToVerifySenderHasReceivedAckAndIfNotSendAgain();
		}
	}
	
	public void processPacket(final FilePacketContents packet) {
		
		if(!receivingWindow.isInWindow(packet.getSeqNum())) {
			sendDuplicateAck();
			return;
		}
		
		final int packetNumber = getPacketNumber(packet.getSeqNum());
		
//		System.out.println("Received packet with seqNum " + packet.getSeqNum() + " and PacketNum " + packetNumber);
		
		packetTracker.savePacket(packet.getDataBytes(), packetNumber);
		if(packet.isLastPacket()) {
			recLastPacket = true;
		}
		
		if (packet.getSeqNum() != receivingWindow.getSubsequentLargestConsecutivePacketReceived()) {
			sendDuplicateAck();
		} else {
			setLFRToHighestConsAck(packetNumber);
			sendAck();
			lastAckedSeqNumPacNumPair = new int[] { packet.getSeqNum(), packetNumber };
		}
	}
	
	public int getPacketNumber(final int seqNumber) {
		int packetNumber;
		if (lastAckedSeqNumPacNumPair[0] == -1) {
			packetNumber = seqNumber;
			return packetNumber;
		} else {
			int lastSeenSeqNumber = lastAckedSeqNumPacNumPair[0];
			int lastSeenPacketNumber = lastAckedSeqNumPacNumPair[1];
			
			if (seqNumber > lastSeenSeqNumber) {
				packetNumber = lastSeenPacketNumber + (seqNumber - lastSeenSeqNumber);
			} else {
				packetNumber = lastSeenPacketNumber + (ReceivingWindow.SEQUENCE_NUMBER_SPACE - lastSeenSeqNumber + seqNumber);
			}
			
			return packetNumber;
		}
	}
	
	public void sendDuplicateAck() {
		duplicateAck = true;
		sendAck();
		duplicateAck = false;
	}
	
	public void sendAck() {
//		System.out.println("sending an ack with seqnum " + receivingWindow.getLargestConsecutivePacketReceived());
		recAllPackets = recLastPacket && packetTracker.allPacketsUpToMostRecentlyArrivedPacketReceived();
		final DatagramPacket ack = ackMaker.makePacket(recAllPackets, duplicateAck, receivingWindow.getLargestConsecutivePacketReceived());
		ackSender.sendPacket(ack);
	}
	
	public void setLFRToHighestConsAck(final int packetNumber) {
		if(packetNumber == 0) {
			receivingWindow.setLargestConsecutivePacketReceived(0);
		} else {
			final int highestConPacketAccepted = packetTracker.getHighestConsAccepFilePacket();
			receivingWindow.setLargestConsecutivePacketReceived(highestConPacketAccepted%ReceivingWindow.SEQUENCE_NUMBER_SPACE);
		}
	}
	
	public void saveFile() {
		
		File file;
		boolean overWritten = false;
    	
        try {
        	file = new File(absoluteFilePath);
			if(!file.createNewFile()){
				overWritten = true;
			}
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFilePath, "rw");
		
			for(Map.Entry<Integer, byte[]> entry : packetTracker.getAllReceivedPackets().entrySet()) {
				randomAccessFile.write(entry.getValue());
				//TODO: make into something like below
				//receivedPackets.entrySet().stream().map(element -> element.getValue()).collect(collector)
			}
			
			if (overWritten) {
				System.out.println("> File " + fileName + " saved in " + file.getAbsolutePath() + ". "
						+ "Note: there was already a file, it was overwritten!");
			} else {
				System.out.println("> File " + fileName + " saved in " + file.getAbsolutePath() + ".");
			}
			
			randomAccessFile.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
}
