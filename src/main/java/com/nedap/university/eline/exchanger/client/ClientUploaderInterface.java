package com.nedap.university.eline.exchanger.client;

import java.io.File;
import java.net.DatagramSocket;

public interface ClientUploaderInterface {
	
	public void letClientUploadFile();
	
	public File getUserSelectedFile();
}
