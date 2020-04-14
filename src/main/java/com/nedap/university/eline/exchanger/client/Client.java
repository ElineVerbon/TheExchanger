package com.nedap.university.eline.exchanger.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.nedap.university.eline.exchanger.communication.CommunicationMessages;

public class Client {
	
	private ClientUploaderInterface uploader;
	private ClientListAsker listAsker;
	private ClientDownloader downloader;
	private ClientRemover remover;
	
	public Client(final ClientUploaderInterface uploader, final ClientListAsker listAsker, 
			final ClientDownloader downloader, ClientRemover remover) {
	    this.uploader = uploader;
	    this.listAsker = listAsker;
	    this.downloader = downloader;
	    this.remover = remover;
	}
	
    public static void main(String[] args) {
    	
		try {
//			final InetAddress serverAddress = InetAddress.getLocalHost();
			final int generalServerPort = 8080;
			final String hostname = "nu-pi-stefan";
			final InetAddress serverAddress = InetAddress.getByName(hostname);
			ClientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			
			ClientUploader uploader = new ClientUploader(generalServerPort, serverAddress);
			ClientListAsker listAsker = new ClientListAsker(generalServerPort, serverAddress);
			ClientDownloader downloader = new ClientDownloader(generalServerPort, serverAddress);
			ClientRemover remover = new ClientRemover(generalServerPort, serverAddress);
			Client client = new Client(uploader, listAsker, downloader, remover);
			
	    	client.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() {
    	ClientTUI.showMessage("Do you want to upload, get a list of files, download, upload or exit? (d, l, u or e)");
    	String usersChoice = ClientTUI.getChoice();
    	String result = processChoice(usersChoice);
    	
    	if (result == null) {
    		System.out.println("Something went wrong!");
    	}
    	
    	while (result != CommunicationMessages.EXIT) {
            ClientTUI.showMessage("What do you want to do next? (Upload u, download d, or exit s.)");
            usersChoice = ClientTUI.getChoice();
            result = processChoice(usersChoice);
    	} 
    }
    
	public String processChoice(String usersChoice) {
		if(usersChoice.equals(CommunicationMessages.UPLOAD)) {
			uploader.letClientUploadFile();
			return CommunicationMessages.UPLOAD;
		} else if(usersChoice.equals(CommunicationMessages.LIST)) {
			listAsker.letClientAskForList();
			return CommunicationMessages.LIST;
		} else if(usersChoice.equals(CommunicationMessages.DOWNLOAD)) {
			downloader.letClientDownloadFile();
			return CommunicationMessages.DOWNLOAD;
		} else if(usersChoice.equals(CommunicationMessages.WITHDRAW)) {
			remover.letClientRemoveFile();
			return CommunicationMessages.WITHDRAW;
		} else if(usersChoice.equals(CommunicationMessages.EXIT)) {
			return CommunicationMessages.EXIT;
		}
		return null;
	}
}


