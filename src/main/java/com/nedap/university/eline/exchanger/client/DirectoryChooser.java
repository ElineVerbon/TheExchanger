package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.nedap.university.eline.exchanger.exceptions.UserQuitToMainMenuException;

public class DirectoryChooser {

	private String absolutePath;
	private String directory;
	private String savedPath;
	private boolean dirSaved;
	private boolean saidYesToOverwrite;
	
	public DirectoryChooser() {
		this.absolutePath = "";
		this.directory = "";
		this.savedPath = "";
		this.dirSaved = false;
		this.saidYesToOverwrite = false;
	}
	
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public boolean chooseDirectory(final String message, final String fileName) throws UserQuitToMainMenuException {
		
		try {
			boolean correctInput = false;
			directory = "";
			
			while (!correctInput) {
				directory = getDirectory(message);
				
				absolutePath = directory + File.separator + fileName;
				
				if (Files.exists(Paths.get(absolutePath)) && !saidYesToOverwrite) {
					if (!ClientTUI.getBoolean("The file " + absolutePath + " already exists, do you want to replace it? (y / n)")) {
						continue;
					}
					Files.delete(Paths.get(absolutePath));
				}
				correctInput = true;
			}
			saidYesToOverwrite = false;
			savedPath = directory;
			dirSaved = true;
		} catch (IOException e) {
			System.out.println("Could not check the path and file while user chooses a directory.");
		}
		return true;
	}
    
    private String getDirectory(final String message) throws UserQuitToMainMenuException {
    	
    	if (dirSaved) {
			if (ClientTUI.getBoolean("Do you want to save the file to the previous path (" + savedPath + ")? (y / n)")) {
				saidYesToOverwrite = true;
				return savedPath;
			}
		}
    	
    	ClientTUI.showMessage(message);
    	String absoluteFilePathDir = ClientTUI.getString();
    	checkForExit(absoluteFilePathDir);
    	File directory = new File(absoluteFilePathDir);

    	while (!directory.isDirectory()) {
    		ClientTUI.showMessage("");
    		ClientTUI.showMessage("The String you typed is not a directory on this computer. "
    				+ "Please try again. (Type x to return to the main menu.)");
    		absoluteFilePathDir = ClientTUI.getString();
    		checkForExit(absoluteFilePathDir);
        	directory = new File(absoluteFilePathDir);
    	}
    	savedPath = absoluteFilePathDir;
    	
    	return absoluteFilePathDir;
    }
    
    private void checkForExit(final String userInput) throws UserQuitToMainMenuException {
    	if (userInput.equals("x")) {
    		throw new UserQuitToMainMenuException();
    	}
    }

}
