package com.nedap.university.eline.exchanger.client;

import java.io.File;

public interface ClientUploaderInterface {
	
	public void uploadFile();
	
	public void startSending(final File file, final int port);

	public File getUserSelectedFile();
}
