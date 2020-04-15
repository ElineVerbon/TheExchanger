package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import com.nedap.university.eline.exchanger.communication.CommunicationStrings;

public abstract class AbstractClientExecutor {
	
	private int generalServerPort;
	private InetAddress serverAddress;
	
	public AbstractClientExecutor(int serverPort, InetAddress serverAddress) {
		this.generalServerPort = serverPort;
		this.serverAddress = serverAddress;
	}
	
	public int getGeneralServerPort() {
		return generalServerPort;
	}
	
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	public int letServerKnowWhatTheClientWantsToDoAndGetAServerPort(final byte[] choiceByte, final byte[] dataBytes, final DatagramSocket socket) {
    	DatagramPacket packet = makeDataPacket(choiceByte, dataBytes, serverAddress, generalServerPort);
    	sendToServer(packet, socket);
    	DatagramPacket response = receivePacket(socket);
    	return response.getPort();
    }
    
    public DatagramPacket makeDataPacket(final byte[] choiceByte, final byte[] dataBytes, 
    		final InetAddress serverAddress, final int serverPort) {
		byte[] packetBytes = new byte[dataBytes.length + choiceByte.length];
		System.arraycopy(choiceByte, 0, packetBytes, 0, choiceByte.length);
		System.arraycopy(dataBytes, 0, packetBytes, choiceByte.length, dataBytes.length);
		return new DatagramPacket(packetBytes, packetBytes.length, serverAddress, serverPort);
	}
    
    public void sendToServer(DatagramPacket packet, DatagramSocket socket) {
    	try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Could not send the choice to the server.");
		}
    }
    
    public DatagramPacket receivePacket(DatagramSocket socket) {
		DatagramPacket response = null;
    	try {
			response = new DatagramPacket(new byte[1], 1);
			socket.receive(response);
		} catch (IOException e) {
			System.out.println("Receiving a message went wrong. Error message: " + e.getMessage());
		}
		return response;
    }
    
    public File getUserSelectedLocalFile(final String message) {
    	ClientTUI.showMessage(message);
    	String absoluteFilePath = ClientTUI.getString();
    	File file = new File(absoluteFilePath);

    	while (!(file.exists() || absoluteFilePath.equals("x"))) {
    		ClientTUI.showMessage("");
    		ClientTUI.showMessage("The file could not be found. Please try again. (Type x to return to the main menu.)");
    		absoluteFilePath = ClientTUI.getString();
        	file = new File(absoluteFilePath);
    	}
    	
    	return file;
    }

    public String letUserEnterTheNameOfAFileOnTheServer(final String message, final ClientListAsker listAsker) {
		ClientTUI.showMessage("Please be patient, retrieving all files present on the server.");
		String fileNamesOnServer = listAsker.letClientAskForList();
    	ClientTUI.showMessage(message);
    	String fileName = ClientTUI.getString();
    	
    	List<String> fileNames = Arrays.asList(fileNamesOnServer.split(CommunicationStrings.SEPARATION_TWO_FILES));
    	while (!(fileNames.contains(fileName) || fileName.equals("x"))) {
    		ClientTUI.showMessage("The file name is not known on the server. Please try again. (Type x to return to the main menu.)");
    		fileName = ClientTUI.getString();
    	}
    	
    	//TODO, would like to check whether it will overwrite another file ont he desktop.
    	
    	return fileName;
    }

}
