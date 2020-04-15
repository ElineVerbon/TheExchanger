package com.nedap.university.eline.exchanger.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ServerHandlerDownloadingClient {
	
	private Map<String, Thread> startedThreads;
	private List<String> filesWithAnAnterruptedThread;
	
	public ServerHandlerDownloadingClient() {
		this.startedThreads = new HashMap<>();
		this.filesWithAnAnterruptedThread = new ArrayList<>();
	}

	public void letUserDownloadFile(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	byte[] choiceByte = Arrays.copyOfRange(packet.getData(), 0, 1);
	    	new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort);
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			thisCommunicationsSocket.send(new DatagramPacket(choiceByte, choiceByte.length, clientAddress, clientPort));
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			File file = new File(Server.ACCESSIBLE_FOLDER + fileName);
			final byte[] fileBytes = Files.readAllBytes(file.toPath());
			
			FileSendManager manager = new FileSendManager(fileBytes, clientAddress, clientPort, thisCommunicationsSocket, fileName);
			startAndSaveNewThreadToSendFile(fileName, manager);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startAndSaveNewThreadToSendFile(final String fileName, final FileSendManager manager) {
		Thread thread = new Thread(() -> manager.sendFile());
		thread.start();
		startedThreads.put(fileName, thread);
	}
	
	public byte[] getListOfFiles() {
		byte[] fileList = null;
		try {
			String allFiles = "";
			File directory = new File(Server.ACCESSIBLE_FOLDER);
			for (File file : directory.listFiles()) {
				allFiles = allFiles + file.getName() + CommunicationStrings.SEPARATION_NAME_SIZE + file.length() + CommunicationStrings.SEPARATION_TWO_FILES;
			}
			fileList = allFiles.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		}
		return fileList;
	}
	
	public void pauseAThread(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			
			byte[] outcome;
			
			if (!startedThreads.containsKey(fileName)) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.NO_SUCH_THREAD);
			} else if (!(startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
					|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.FINISHED);
			} else if (filesWithAnAnterruptedThread.contains(fileName)) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.INTERRUPTED);
			} else {
				startedThreads.get(fileName).interrupt();
				filesWithAnAnterruptedThread.add(fileName);
				System.out.println(filesWithAnAnterruptedThread.get(0));
				outcome = CommunicationStrings.toBytes(CommunicationStrings.SUCCESS);
			}
			
			thisCommunicationsSocket.send(new DatagramPacket(outcome, outcome.length, clientAddress, clientPort));
			//TODO wait to see whether it arrived?
			thisCommunicationsSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resumeAThread(final DatagramPacket packet) {
		try {
			final InetAddress clientAddress = packet.getAddress();
	    	final int clientPort = packet.getPort();
	    	DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			
			byte[] fileNameBytes = Arrays.copyOfRange(packet.getData(), 1, packet.getLength());
			String fileName = new String(fileNameBytes);
			
			byte[] outcome;
			
			if (!startedThreads.containsKey(fileName)) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.NO_SUCH_THREAD);
			}  else if (!(startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
					|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.FINISHED);
			} else if (!(filesWithAnAnterruptedThread.contains(fileName))) {
				outcome = CommunicationStrings.toBytes(CommunicationStrings.INTERRUPTED);
			} else {
				startedThreads.get(fileName).interrupt();
				filesWithAnAnterruptedThread.remove(fileName);
				outcome = CommunicationStrings.toBytes(CommunicationStrings.SUCCESS);
			}
			
			thisCommunicationsSocket.send(new DatagramPacket(outcome, outcome.length, clientAddress, clientPort));
			//TODO wait to see whether it arrived?
			thisCommunicationsSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
