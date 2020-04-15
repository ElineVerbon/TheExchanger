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
	private ClientTerminator terminator;
	
	public Client(final ClientUploader uploader, final ClientListAsker listAsker, final ClientDownloader downloader, 
			final ClientRemover remover, final ClientReplacer replacer, final ClientPauser pauser, 
			final ClientResumer resumer, final ClientTerminator terminator) {
	    this.uploader = uploader;
	    this.listAsker = listAsker;
	    this.downloader = downloader;
	    this.remover = remover;
	    this.replacer = replacer;
	    this.pauser = pauser;
	    this.resumer = resumer;
	    this.terminator = terminator;
	}
	
    public static void main(String[] args) {
    	
    	ClientTUI.showMessage("Client is starting up, please be patient.");
    	
		try {
			final InetAddress serverAddress = InetAddress.getLocalHost();
			final int generalServerPort = 8080;
//			final String hostname = "nu-pi-stefan";
//			final InetAddress serverAddress = InetAddress.getByName(hostname);
//			ClientTUI.showMessage("Connection established with \"" + hostname + "\"."); 
			
			ChoiceCommunicator communicator = new ChoiceCommunicator(generalServerPort, serverAddress);
			ClientListAsker listAsker = new ClientListAsker(communicator);
			ClientDownloader downloader = new ClientDownloader(communicator, listAsker);
			ClientUploader uploader = new ClientUploader(communicator);
			ClientPauser pauser = new ClientPauser(communicator);
			ClientRemover remover = new ClientRemover(communicator, listAsker);
			ClientReplacer replacer = new ClientReplacer(communicator, listAsker);
			ClientResumer resumer = new ClientResumer(communicator);
			ClientTerminator terminator = new ClientTerminator(communicator);
			
			Client client = new Client(uploader, listAsker, downloader, remover, replacer, 
					pauser, resumer, terminator);
			
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
					pauser.letClientPauseDownload();
				} else if(usersChoice.equals(CommunicationStrings.CONTINUE)) {
					resumer.letClientResumeDownload();
				} else if(usersChoice.equals(CommunicationStrings.HELP)) {
					printHelpMenu();
				}
			} catch (UserQuitToMainMenuException e) {
			} catch (SocketTimeoutException e) {
				ClientTUI.showMessage("Action was aborted as there was no response from the server.");
			}
			usersChoice = ClientTUI.getChoice();
		}
		terminator.endProgram();
    }
	
	public void printHelpMenu() {
		ClientTUI.showMessage("Type one of the following single characters, followed by hitting enter to execute the corresponding action.\n"
				+ " u: upload a file to the server\n d: download a file from the server\n w: withdraw (remove) a file from the server\n"
				+ " r: replace a file on the server with a local file\n p: pause the download of a file\n c: continue the paused download of a file\n"
				+ " e: exit the program");
	}
}


