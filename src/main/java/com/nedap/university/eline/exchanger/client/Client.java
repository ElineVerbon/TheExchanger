package com.nedap.university.eline.exchanger.client;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;

public class Client {
	
	private ClientUploader uploader;
	private ClientListAsker listAsker;
	private ClientDownloader downloader;
	private ClientRemover remover;
	private ClientReplacer replacer;
	private ClientTerminator terminator;
	
	public Client(final ClientUploader uploader, final ClientListAsker listAsker, final ClientDownloader downloader, 
			final ClientRemover remover, final ClientReplacer replacer, final ClientTerminator terminator) {
	    this.uploader = uploader;
	    this.listAsker = listAsker;
	    this.downloader = downloader;
	    this.remover = remover;
	    this.replacer = replacer;
	    this.terminator = terminator;
	}
	
    public static void main(String[] args) {
    	
    	ClientTUI.showMessage("Client is starting up, please be patient.");
    	
		try {
//			final InetAddress serverAddress = InetAddress.getLocalHost();
			final int generalServerPort = 8080;
			final String hostname = "nu-pi-stefan";
			final InetAddress serverAddress = InetAddress.getByName(hostname);
			ClientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			
			ChoiceCommunicator communicator = new ChoiceCommunicator(generalServerPort, serverAddress);
			ClientListAsker listAsker = new ClientListAsker(communicator);
			ClientDownloader downloader = new ClientDownloader(communicator, listAsker);
			ClientUploader uploader = new ClientUploader(communicator);
			ClientRemover remover = new ClientRemover(communicator, listAsker);
			ClientReplacer replacer = new ClientReplacer(communicator, listAsker);
			ClientTerminator terminator = new ClientTerminator(communicator);
			
			Client client = new Client(uploader, listAsker, downloader, remover, replacer, terminator);
			
	    	client.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void start() {
    	String usersChoice = ClientTUI.getChoice();
		
		while (!usersChoice.equals(CommunicationStrings.EXIT)) {
			try {
				if(usersChoice.equals(CommunicationStrings.UPLOAD)) {
					uploader.letClientUploadFile();
				} else if(usersChoice.equals(CommunicationStrings.LIST)) {
					listAsker.letClientAskForList();
				} else if(usersChoice.equals(CommunicationStrings.DOWNLOAD)) {
					downloader.letClientDownloadFile();
				} else if(usersChoice.equals(CommunicationStrings.WITHDRAW)) {
					remover.letClientRemoveFile();
				} else if(usersChoice.equals(CommunicationStrings.REPLACE)) {
					replacer.letClientReplaceFile();
				} else if(usersChoice.equals(CommunicationStrings.PAUSE)) {
					downloader.letClientPauseDownload();
				} else if(usersChoice.equals(CommunicationStrings.CONTINUE)) {
					downloader.letClientResumeDownload();
				} else if(usersChoice.equals(CommunicationStrings.STATISTICS)) {
					String uploadStatistics = uploader.getStatistics() + replacer.getStatistics();
					String downloadStatistics = downloader.letClientGetStatistics();
					TransferStatistics.printStatistics(uploadStatistics, downloadStatistics);
				} else if(usersChoice.equals(CommunicationStrings.HELP)) {
					ClientTUI.printHelpMenu();
				}
			} catch (UserQuitToMainMenuException e) {
			} catch (SocketTimeoutException e) {
				ClientTUI.showMessage("Action was aborted as there was no response from the server.");
			}
			usersChoice = ClientTUI.getChoice();
		}
		terminator.endProgram();
    }
}


