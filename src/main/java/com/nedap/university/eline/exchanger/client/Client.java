package com.nedap.university.eline.exchanger.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;

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
//			final InetAddress serverAddress = InetAddress.getLocalHost();
			final int generalServerPort = 8080;
			final String hostname = "nu-pi-stefan";
			final InetAddress serverAddress = InetAddress.getByName(hostname);
			ClientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			
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
    	ClientTUI.showMessage("Do you want to download, upload or exit? (d, u or e)");
    	String usersChoice = ClientTUI.getChoice();
    	result = processChoice(usersChoice);
    	
    	while (result != Result.EXIT) {
            ClientTUI.showMessage("What do you want to do next? (Upload u, download d, or exit s.)");
            usersChoice = ClientTUI.getChoice();
            result = processChoice(usersChoice);
    	} 
    }
    
	Result processChoice(String usersChoice) {
		if(usersChoice.equals(CommunicationMessages.UPLOAD)) {
			uploader.letClientUploadFile();
			return Result.UPLOAD_STARTED;
		} else if(usersChoice.equals(CommunicationMessages.EXIT)) {
			return Result.EXIT;
		}
		return Result.ERROR;
	}
}


