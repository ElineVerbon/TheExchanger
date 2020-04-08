package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

import javax.swing.JFileChooser;

import com.nedap.university.eline.exchanger.shared.SendingCommunicator;

public class ClientDownloader extends AbstractClientExecutor {
	private ClientTUI clientTUI;
	private int serverPort;
	private InetAddress serverAddress;
	private DatagramSocket socket;
	
	public ClientDownloader(ClientTUI clientTUI, int serverPort, InetAddress serverAddress, DatagramSocket socket) {
		this.clientTUI = clientTUI;
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
		this.socket = socket;
	}
	
    public void downloadFile() {
		byte[] choiceIndicator = new byte[] {(byte) 'd'};
		File toBeDownloadedFile = getUserSelectedFile();
		byte[] fileNameBytes = toBeDownloadedFile.getName().getBytes();
		
		int port = getCorrectServerPort(choiceIndicator, fileNameBytes, serverAddress, serverPort, socket);
		
		new Thread(() -> startReceiving(toBeDownloadedFile, port)).start();
	}
	
	public void startReceiving(final File file, final int port) {
		try {
			new SendingCommunicator(Files.readAllBytes(file.toPath()), serverAddress, port, socket).uploadFile();
		} catch (IOException e) {
			clientTUI.showMessage("File could not be converted to byte. Error message: " + e.getMessage());
		}
		System.out.println("File " + file.getName() + " was successfully uploaded onto the pi.");
	}
    
    private File getUserSelectedFile() {
    	clientTUI.showMessage("Please select a file to upload.");
    	JFileChooser jfc = new JFileChooser();
        jfc.showDialog(null,"Please Select the File");
        jfc.setVisible(true);
        File file = jfc.getSelectedFile();
        System.out.println("You selected the file " + file.getName());
        return file;
    }
    
    private void getFilesFromPi() {
    	//TODO write method to get files from the Pi
    }
}
