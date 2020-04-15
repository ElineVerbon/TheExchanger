package com.nedap.university.eline.exchanger.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public class Client {
	
	private ClientUploader uploader;
	private ClientListAsker listAsker;
	private ClientDownloader downloader;
	private ClientRemover remover;
	private ClientReplacer replacer;
	private ClientPauser pauser;
	private ClientResumer resumer;
	
	public Client(final ClientUploader uploader, final ClientListAsker listAsker, final ClientDownloader downloader, 
			final ClientRemover remover, final ClientReplacer replacer, final ClientPauser pauser, final ClientResumer resumer) {
	    this.uploader = uploader;
	    this.listAsker = listAsker;
	    this.downloader = downloader;
	    this.remover = remover;
	    this.replacer = replacer;
	    this.pauser = pauser;
	    this.resumer = resumer;
	}
	
    public static void main(String[] args) {
    	
    	ClientTUI.showMessage("Client is starting up, please be patient.");
    	
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
			ClientReplacer replacer = new ClientReplacer(generalServerPort, serverAddress);
			ClientPauser pauser = new ClientPauser(generalServerPort, serverAddress);
			ClientResumer resumer = new ClientResumer(generalServerPort, serverAddress);
			Client client = new Client(uploader, listAsker, downloader, remover, replacer, pauser, resumer);
			
	    	client.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() {
    	String usersChoice = ClientTUI.getChoice();
    	String result = processChoice(usersChoice);
    	
    	while (result != CommunicationStrings.EXIT) {
            usersChoice = ClientTUI.getChoice();
            result = processChoice(usersChoice);
    	} 
    }
    
	public String processChoice(String usersChoice) {
		if(usersChoice.equals(CommunicationStrings.UPLOAD)) {
			uploader.letClientUploadFile();
			return CommunicationStrings.UPLOAD;
		} else if(usersChoice.equals(CommunicationStrings.LIST)) {
			listAsker.letClientAskForList();
			return CommunicationStrings.LIST;
		} else if(usersChoice.equals(CommunicationStrings.DOWNLOAD)) {
			downloader.letClientDownloadFile();
			return CommunicationStrings.DOWNLOAD;
		} else if(usersChoice.equals(CommunicationStrings.WITHDRAW)) {
			remover.letClientRemoveFile();
			return CommunicationStrings.WITHDRAW;
		} else if(usersChoice.equals(CommunicationStrings.REPLACE)) {
			replacer.letClientReplaceFile();
			return CommunicationStrings.REPLACE;
		} else if(usersChoice.equals(CommunicationStrings.PAUSE)) {
			pauser.letClientPauseDownload();
			return CommunicationStrings.PAUSE;
		} else if(usersChoice.equals(CommunicationStrings.CONTINUE)) {
			resumer.letClientResumeDownload();
			return CommunicationStrings.CONTINUE;
		} else if(usersChoice.equals(CommunicationStrings.EXIT)) {
			return CommunicationStrings.EXIT;
		}
		printHelpMenu();
		return CommunicationStrings.HELP;
	}
	
	public void printHelpMenu() {
		ClientTUI.showMessage("Type one of the following single characters, followed by hitting enter to execute the corresponding action.\n"
				+ " u: upload a file to the server\n d: download a file from the server\n w: withdraw (remove) a file from the server\n"
				+ " r: replace a file on the server with a local file\n p: pause the download of a file\n c: continue the paused download of a file\n"
				+ " e: exit the program");
	}
}


