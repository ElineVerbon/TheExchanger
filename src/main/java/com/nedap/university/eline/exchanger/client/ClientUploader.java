package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

import com.nedap.university.eline.exchanger.shared.SendingWindowTransmitter;

public class ClientUploader implements Runnable {

	private ClientTUI clientTUI;
	private int serverPort;
	private InetAddress serverAddress;
	private DatagramSocket socket;
	
	public ClientUploader(ClientTUI clientTUI, int serverPort, InetAddress serverAddress, DatagramSocket socket) {
		this.clientTUI = clientTUI;
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
		this.socket = socket;
	}
	
	@Override
	public void run() {
		startTransmitter(getFile());
	}
	
	public void startTransmitter(File file) {
		//TODO start a new thread here?
		try {
			new SendingWindowTransmitter(Files.readAllBytes(file.toPath()), serverAddress, serverPort, socket).uploadFile();
		} catch (IOException e) {
			clientTUI.showMessage("File could not be converted to byte. Error message: " + e.getMessage());
		}
	}
    
    // get files from resources folder
    private File getFile() {
    	clientTUI.showMessage("Please select a file to upload.");
    	JFileChooser jfc = new JFileChooser();
        jfc.showDialog(null,"Please Select the File");
        jfc.setVisible(true);
        File file = jfc.getSelectedFile();
        System.out.println("You selected the file " + file.getName());
        return file;
    }
}
