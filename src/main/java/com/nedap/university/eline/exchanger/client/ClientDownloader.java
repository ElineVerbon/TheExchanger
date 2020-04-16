package com.nedap.university.eline.exchanger.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.DuplicateDownloadException;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;
import com.nedap.university.eline.exchanger.manager.FileReceiveManager;

public class ClientDownloader {
	
	private ClientListAsker listAsker;
	private ChoiceCommunicator communicator;
	
	private DirectoryChooser directoryChooser;
	private FileChooser fileChooser;
	private Map<String, Thread> startedThreads;
	private List<String> filesWithAnAnterruptedDownloadThread;
	private String fileName;
	
	public ClientDownloader(final ChoiceCommunicator communicator, final ClientListAsker asker) {
		this.listAsker = asker;
		this.communicator = communicator;
		directoryChooser = new DirectoryChooser();
		fileChooser = new FileChooser();
		startedThreads = new HashMap<>();
		this.filesWithAnAnterruptedDownloadThread = new ArrayList<>();
	}
	
	public void letClientDownloadFile() throws UserQuitToMainMenuException, SocketTimeoutException {
		fileName = fileChooser.letUserEnterTheNameOfAFileOnTheServer("Please type the name of one of file you want to download. "
				+ "Note: you need to type the entire file name, including extension.", listAsker);
	
		downloadFile();
		boolean moreDownloads = ClientTUI.getBoolean("Do you want to download another file?");
		
		while(moreDownloads) {
			fileName = fileChooser.letUserEnterAnotherName();
			downloadFile();
			moreDownloads = ClientTUI.getBoolean("Do you want to download another file?");
		}
	}
	
	private void downloadFile() throws UserQuitToMainMenuException, SocketTimeoutException  {
		
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.DOWNLOAD);
			
			//TODO add max waiting time for the receive method in getNewServerPort()!
						
			byte[] fileNameBytes = fileName.getBytes();
			
			checkForDuplicateDownload();
			
			directoryChooser.chooseDirectory("Please type the directory in which you want to save the downloaded file.", fileName);
			
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes, thisCommunicationsSocket);
			final int specificServerPort = response.getPort();
			
			FileReceiveManager manager = new FileReceiveManager(thisCommunicationsSocket, communicator.getServerAddress(), specificServerPort, directoryChooser.getDirectory(), fileName);
			startAndSaveNewThreadToReceiveFile(fileName, manager);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to download a file failed.");
		} catch (DuplicateDownloadException e) {
			ClientTUI.showMessage("A thread is currently running to download this file. The download will not be started to prevent duplicate threads."
					+ " You can download it again once the currently running download is finished.");
		}
	}
	
	private void startAndSaveNewThreadToReceiveFile(final String fileName, final FileReceiveManager manager) {
		Thread thread = new Thread(manager);
		thread.start();
		startedThreads.put(fileName, thread);
	}
	
	private void checkForDuplicateDownload() throws DuplicateDownloadException {
		if (startedThreads.containsKey(fileName)) {
			if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
					|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				throw new DuplicateDownloadException();
			}
		}
	}
	
	public void letClientPauseDownload() throws SocketTimeoutException {
		try {
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.PAUSE);
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()
			
			ClientTUI.showMessage("Please type the name of name of the file for which you want to pause"
					+ " the download and hit enter. Note: you need to type the entire file name, including extension.");
			String fileName = ClientTUI.getString();
			byte[] fileNameBytes = fileName.getBytes();
			
			pauseOwnThreads(fileName);
			
			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes,thisCommunicationsSocket);
	    	byte[] responseBytes = response.getData();
			
	    	updatePausingUser(responseBytes);
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		}
	}
	
	private void pauseOwnThreads(final String fileName) {
		if (startedThreads.containsKey(fileName)) {
			if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
				|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				if (!(filesWithAnAnterruptedDownloadThread.contains(fileName))) {
					startedThreads.get(fileName).interrupt();
					filesWithAnAnterruptedDownloadThread.add(fileName);
				}
			}
		}
	}
	
	private void updatePausingUser(final byte[] responseBytes) {
		String serversResponse = new String(responseBytes);
    	if (serversResponse.equals(CommunicationStrings.NO_SUCH_THREAD)) { 
			ClientTUI.showMessage("No thread known for this filename.");
		} else if (serversResponse.equals(CommunicationStrings.FINISHED)) { 
			ClientTUI.showMessage("The download was already finished.");
		} else if (serversResponse.equals(CommunicationStrings.INTERRUPTED)) {
			ClientTUI.showMessage("The download was already paused.");
		} if (serversResponse.equals(CommunicationStrings.SUCCESS)) {
			ClientTUI.showMessage("The download was successfully paused.");
		}
	}
	
	public void letClientResumeDownload() throws SocketTimeoutException {
		try {
			final boolean anyPausedFiles = showPausedFilesIfAny();
			if (!anyPausedFiles) { 
				return; 
			}
		
			ClientTUI.showMessage("Please type the name of name of the file with a paused download that you want to resume"
					+ " and hit enter. Note: you need to type the entire file name, including extension.");
			String fileName = ClientTUI.getString();
			byte[] fileNameBytes = fileName.getBytes();
			
			byte[] choiceIndicator = CommunicationStrings.toBytes(CommunicationStrings.CONTINUE);
			final DatagramSocket thisCommunicationsSocket = new DatagramSocket();

			DatagramPacket response = communicator.communicateChoiceToServer(choiceIndicator, fileNameBytes,thisCommunicationsSocket);
			
			resumeOwnPausedThread(fileName);
	    	
			byte[] responseBytes = response.getData();
	    	updateResumingUser(responseBytes);
	    	
		} catch (SocketException e) {
			ClientTUI.showMessage("Opening a socket to remove a file failed.");
		}
	}
	
	private boolean showPausedFilesIfAny() {
		if (filesWithAnAnterruptedDownloadThread.size() == 0) {
			ClientTUI.showMessage("\nThese are no files of which the download is currently paused. Returning to main menu.");
			return false;
		}
		ClientTUI.showMessage("These are the files of which the download is currently paused:");
		for (String fileName : filesWithAnAnterruptedDownloadThread) {
			ClientTUI.showMessage(fileName + "\n");
		}
		return true;
	}
	
	private void updateResumingUser(final byte[] responseBytes) {
		String serversResponse = new String(responseBytes);
    	if (serversResponse.equals(CommunicationStrings.NO_SUCH_THREAD)) { 
			ClientTUI.showMessage("No thread known for this filename.");
		} else if (serversResponse.equals(CommunicationStrings.FINISHED)) { 
			ClientTUI.showMessage("The download was already finished.");
		} else if (serversResponse.equals(CommunicationStrings.INTERRUPTED)) {
			ClientTUI.showMessage("The download was not paused.");
		} if (serversResponse.equals(CommunicationStrings.SUCCESS)) {
			ClientTUI.showMessage("The download was successfully resumed.");
		}
	}
	
	private void resumeOwnPausedThread(final String fileName) {
		if (startedThreads.containsKey(fileName)) {
			if ((startedThreads.get(fileName).getState() == Thread.State.RUNNABLE 
				|| startedThreads.get(fileName).getState() == Thread.State.TIMED_WAITING)) {
				if (filesWithAnAnterruptedDownloadThread.contains(fileName)) {
					startedThreads.get(fileName).interrupt();
					filesWithAnAnterruptedDownloadThread.remove(fileName);
				}
			}
		}
	}
}
