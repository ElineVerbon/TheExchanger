package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

import javax.swing.JFileChooser;

import com.nedap.university.eline.exchanger.shared.SendingCommunicator;

public class ClientUploader extends AbstractClientExecutor {

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
	
    public void uploadFile() {
		byte[] choiceIndicator = new byte[] {(byte) 'u'};
		File toBeUploadedFile = getUserSelectedFile();
		byte[] fileNameBytes = toBeUploadedFile.getName().getBytes();
		
		int port = getCorrectServerPort(choiceIndicator, fileNameBytes, serverAddress, serverPort, socket);
		
		new Thread(() -> startSending(toBeUploadedFile, port)).start();
	}
	
	public void startSending(final File file, final int port) {
		try {
			new SendingCommunicator(Files.readAllBytes(file.toPath()), serverAddress, port, socket).uploadFile();
		} catch (IOException e) {
			clientTUI.showMessage("File could not be converted to byte. Error message: " + e.getMessage());
		}
		System.out.println("> Message from a previous upload command: File " + file.getName() + " was successfully uploaded onto the pi.");
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
}
