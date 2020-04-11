package com.nedap.university.eline.exchanger.client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	
	private ClientUploaderInterface uploader;
	private ClientTUI clientTUI;

	enum Result{
    	UPLOAD_STARTED, DOWNLOAD_STARTED, EXIT, ERROR
    }
	
	public Client(final ClientUploaderInterface uploader, final ClientTUI clientTUI) {
	    this.uploader = uploader;	
	    this.clientTUI = clientTUI;
	}
	
    public static void main(String[] args) {
    	
		try {
			final ClientTUI clientTUI = new ClientTUI();
			DatagramSocket socket = new DatagramSocket();
			//final InetAddress serverAddress = InetAddress.getLocalHost();
			final int serverPort = 8080;
			final String hostname = "nu-pi-stefan";
			final InetAddress serverAddress = InetAddress.getByName(hostname);
			clientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			Client client = new Client(new ClientUploader(clientTUI, serverPort, serverAddress, socket), clientTUI);
	    	client.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() {
    	Result result;
    	String usersChoice = clientTUI.getChoice("Do you want to download, upload or exit? (d, u or e)");
    	
    	do {
            result = processChoice(usersChoice);
            usersChoice = clientTUI.getChoice("Do you want to download or upload something else or do you want to exit? (d, u or e)");
    	} while (result != Result.EXIT);
    }
    
	Result processChoice(String usersChoice) {
		if(usersChoice.equals("u")) {
			uploader.uploadFile();
			return Result.UPLOAD_STARTED;
		} else if(usersChoice.equals("e")) {
			return Result.EXIT;
		}
		return Result.ERROR;
	}
}


