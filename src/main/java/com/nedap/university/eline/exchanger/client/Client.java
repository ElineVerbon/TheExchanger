package com.nedap.university.eline.exchanger.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {
	
	private ClientUploaderInterface uploader;

	enum Result{
    	UPLOAD_STARTED, DOWNLOAD_STARTED, EXIT, ERROR
    }
	
	public Client(final ClientUploaderInterface uploader) {
	    this.uploader = uploader;	
	}
	
    public static void main(String[] args) {
    	
		try {
			final InetAddress serverAddress = InetAddress.getLocalHost();
			final int generalServerPort = 8080;
//			final String hostname = "nu-pi-stefan";
//			final InetAddress serverAddress = InetAddress.getByName(hostname);
//			clientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			
			ClientUploader uploader = new ClientUploader(generalServerPort, serverAddress);
			//TODO add downloader and possibly others
			Client client = new Client(uploader);
			
	    	client.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() {
    	Result result;
    	String usersChoice = ClientTUI.getChoice("Do you want to download, upload or exit? (d, u or e)");
    	
    	do {
            result = processChoice(usersChoice);
            usersChoice = ClientTUI.getChoice("Do you want to download or upload something else or do you want to exit? (d, u or e)");
    	} while (result != Result.EXIT);
    }
    
	Result processChoice(String usersChoice) {
		if(usersChoice.equals("u")) {
			uploader.letClientUploadFile();
			return Result.UPLOAD_STARTED;
		} else if(usersChoice.equals("e")) {
			return Result.EXIT;
		}
		return Result.ERROR;
	}
}


