package com.nedap.university.eline.exchanger.manager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;

import com.nedap.university.eline.exchanger.executor.AckSender;
import com.nedap.university.eline.exchanger.executor.FilePacketSender;
import com.nedap.university.eline.exchanger.executor.ReceivedFilePacketTracker;
import com.nedap.university.eline.exchanger.executor.Receiver;
import com.nedap.university.eline.exchanger.packet.FilePacketContents;
import com.nedap.university.eline.exchanger.packet.AckPacketMaker;
import com.nedap.university.eline.exchanger.packet.ChecksumGenerator;
import com.nedap.university.eline.exchanger.window.ReceivingWindow;

public class FileReceiveManager implements Runnable {
	
	private ReceivingWindow receivingWindow;
	private ReceivedFilePacketTracker packetTracker;
	private Receiver receiver;
	private AckPacketMaker ackMaker;
	private AckSender ackSender;
	
	private int[] lastAckedSeqNumPacNumPair = new int[] { -1, -1 };
	public boolean recAllPackets = false;
	private boolean recLastPacket = false;
	private boolean duplicateAck = false;
	
	private String absoluteFilePathDir;
	private String fileName;
	private DatagramSocket socket;
	private volatile boolean flag = true;
	
	public FileReceiveManager(final DatagramSocket socket, final InetAddress sourceAddress, final int sourcePort, 
			final String absoluteFilePathDir, final String fileName) {
		this.socket = socket;
		this.receivingWindow= new ReceivingWindow();
		this.packetTracker = new ReceivedFilePacketTracker();
		receiver = new Receiver(socket);
		this.ackMaker = new AckPacketMaker(sourceAddress, sourcePort);
		this.ackSender = new AckSender(socket);
		
		this.absoluteFilePathDir = absoluteFilePathDir;
		this.fileName = fileName;
    }
	
	public void run() {
		System.out.println("Receiving the file " + fileName + ".");
		
		while(flag && !recAllPackets) {
			try {
				final DatagramPacket packet = receiver.receivePacket(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE);
				processPacket(new FilePacketContents(packet));
			} catch (SocketTimeoutException e) {
				if (Thread.interrupted()) {
					//if socket timed out because user paused the download, wait until time to resume
					handleInterruption();
				}
				else {
					System.out.println("> Receiving the file " + fileName + " failed because the socket connection broke down.");
					return;
				}
			}
		}
		
		boolean done = false;
		while(!done) {
			done = waitToVerifySenderHasReceivedAckAndIfNotSendAgain();
		}
		
		saveFile();
		socket.close();
	}
	
	public void stopRunning() {
		flag = false;
	}
	
	private boolean waitToVerifySenderHasReceivedAckAndIfNotSendAgain() {
		DatagramPacket possiblePacket;
		try {
			possiblePacket = receiver.receivePacket(FilePacketContents.HEADERSIZE + FilePacketContents.DATASIZE, FilePacketSender.timeOutTime * 1000);
		} catch (SocketTimeoutException e) {
			return true;
		}
		
		if(possiblePacket != null) {
			sendDuplicateAck();
			return false;
		} else {
			return true;
		}
	}
	
	private void processPacket(final FilePacketContents packet) {
		
		if (!isPacketIntact(packet)) {
			return;
		}
		
		final int seqNumber = packet.getSeqNum();
		if(!receivingWindow.isInWindow(seqNumber)) {
			sendDuplicateAck();
			return;
		}
		
		final int packetNumber = getPacketNumber(seqNumber);
//		System.out.println("Received packet with seqNum " + packet.getSeqNum() + " and PacketNum " + packetNumber);
		
		packetTracker.savePacket(packet.getDataBytes(), packetNumber);
		if(packet.isLastPacket()) {
			recLastPacket = true;
		}
		
		if (seqNumber != receivingWindow.getSubsequentLargestConsecutivePacketReceived()) {
			sendDuplicateAck();
		} else {
			setLFRToHighestConsAck(packetNumber);
			sendAck();
			lastAckedSeqNumPacNumPair = new int[] { seqNumber, packetNumber };
		}
	}
	
	private boolean isPacketIntact(final FilePacketContents packet) {
		final byte[] givenChecksum = packet.getChecksum();
		final byte[] bytes = packet.getBytes();
		final byte[] bytesWithoutChecksum = Arrays.copyOfRange(bytes, ChecksumGenerator.CHECKSUM_LENGTH, bytes.length);
		final byte[] calculatedChecksum = ChecksumGenerator.getCheckSum(bytesWithoutChecksum);
		return (Arrays.equals(givenChecksum, calculatedChecksum));
	}
	
	private int getPacketNumber(final int seqNumber) {
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
	
	private void sendDuplicateAck() {
		duplicateAck = true;
		sendAck();
		duplicateAck = false;
	}
	
	private void sendAck() {
		recAllPackets = recLastPacket && packetTracker.allPacketsUpToMostRecentlyArrivedPacketReceived();
		final DatagramPacket ack = ackMaker.makePacket(recAllPackets, duplicateAck, receivingWindow.getLargestConsecutivePacketReceived());
		ackSender.sendPacket(ack);
	}
	
	private void setLFRToHighestConsAck(final int packetNumber) {
		if(packetNumber == 0) {
			receivingWindow.setLargestConsecutivePacketReceived(0);
		} else {
			final int highestConPacketAccepted = packetTracker.getHighestConsAccepFilePacket();
			receivingWindow.setLargestConsecutivePacketReceived(highestConPacketAccepted%ReceivingWindow.SEQUENCE_NUMBER_SPACE);
		}
	}
	
	private void saveFile() {
		
		File file;
		boolean overWritten = false;
    	
        try {
        	final String absoluteFilePath = absoluteFilePathDir + File.separator + fileName;
        	file = new File(absoluteFilePath);
			if(!file.createNewFile()){
				overWritten = true;
			}
			RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFilePath, "rw");
		
			for(Map.Entry<Integer, byte[]> entry : packetTracker.getAllReceivedPackets().entrySet()) {
				randomAccessFile.write(entry.getValue());
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
	
	private void handleInterruption() {
		//was interrupted by the user: wait under user wants to resume
		try {
			while (true) {
				Thread.sleep(60*60*1000);
			}
		} catch (InterruptedException e) {
		}
	}
}
