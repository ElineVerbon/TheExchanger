package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;
import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;

public class FileChooser {
	
	private List<String> savedFileNames;
	
	public FileChooser() {
		savedFileNames = new ArrayList<>();
	}
    
    public File getUserSelectedLocalFile(final String message) throws UserQuitToMainMenuException {
    	ClientTUI.showMessage(message);
    	String absoluteFilePath = ClientTUI.getString();
    	checkForExit(absoluteFilePath);
    	File file = new File(absoluteFilePath);

    	while (!(file.exists())) {
    		ClientTUI.showMessage("The file could not be found. Please try again. (Type x to return to the main menu.)");
    		absoluteFilePath = ClientTUI.getString();
    		checkForExit(absoluteFilePath);
        	file = new File(absoluteFilePath);
    	}
    	
    	return file;
    }

    public String letUserEnterTheNameOfAFileOnTheServer(final String message, final ClientListAsker listAsker) throws UserQuitToMainMenuException, SocketTimeoutException {
		ClientTUI.showMessage("To let you choose among the available files, a list of the files on the server will be downloaded first.");
		String fileNamesOnServer = listAsker.letClientAskForList();
    	ClientTUI.showMessage(message);
    	
    	List<String> fileNames = Arrays.asList(fileNamesOnServer.split(CommunicationStrings.SEPARATION_TWO_FILES));
    	final String fileName = getValidInput(fileNames);
    	savedFileNames = fileNames;
    	return fileName;
    }
    
    //can only call this after having called the method above & within the ClientDownloader. (Don't know whether I can enforce that in some way.)
    public String letUserEnterAnotherName() throws UserQuitToMainMenuException {
    	ClientTUI.showMessage("Please enter another file name.");
    	return getValidInput(savedFileNames);
    }
    
    private String getValidInput(final List<String> validFileNames) throws UserQuitToMainMenuException {
    	String fileName = ClientTUI.getString();
    	checkForExit(fileName);
    	
    	while (!(validFileNames.contains(fileName)) || fileName.equals("")) {
    		ClientTUI.showMessage("The file name is not known on the server. Please try again. (Type x to return to the main menu.)");
    		fileName = ClientTUI.getString();
    		checkForExit(fileName);
    	}
    	
    	return fileName;
    }
    
    private void checkForExit(final String userInput) throws UserQuitToMainMenuException {
    	if (userInput.equals("x")) {
    		throw new UserQuitToMainMenuException();
    	}
    }
}
