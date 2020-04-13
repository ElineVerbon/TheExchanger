package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

import javax.swing.JFileChooser;

import com.nedap.university.eline.exchanger.manager.FileSendManager;

public class ClientUploader extends AbstractClientExecutor implements ClientUploaderInterface {

	private int generalServerPort;
	private InetAddress serverAddress;
	
	public ClientUploader(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
    public void letClientUploadFile() {
    	
		try {
			String choice = "u";
			byte[] choiceIndicator = choice.getBytes("UTF-8");
			
			File toBeUploadedFile = getUserSelectedFile();
			String fileName = toBeUploadedFile.getName();
			byte[] fileNameBytes = fileName.getBytes("UTF-8");
			String checkFileName = new String(fileNameBytes);
			System.out.println(checkFileName);
					
			DatagramSocket thisCommunicationsSocket = new DatagramSocket();
			//TODO add max waiting time for the receive method in getNewServerPort()!
			final int thisCommunicationsServerPort = getNewServerPort(choiceIndicator, fileNameBytes, serverAddress, 
					generalServerPort, thisCommunicationsSocket);
			//TODO wait for a response and save it's port number and address. Then start the communication as before.
			
			final byte[] fileBytes = Files.readAllBytes(toBeUploadedFile.toPath());
			new FileSendManager(fileBytes, serverAddress, thisCommunicationsServerPort, thisCommunicationsSocket, fileName).sendFile();
			
		} catch (UnsupportedEncodingException e) {
			System.out.println("The encoding is not supported!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public File getUserSelectedFile() {
    	ClientTUI.showMessage("Please select a file to upload.");
    	JFileChooser jfc = new JFileChooser();
        jfc.showDialog(null,"Please Select the File");
        jfc.setVisible(true);
        File file = jfc.getSelectedFile();
        System.out.println("You selected the file " + file.getName());
        return file;
    }
}
