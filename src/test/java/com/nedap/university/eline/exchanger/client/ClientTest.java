package com.nedap.university.eline.exchanger.client;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nedap.university.eline.exchanger.client.Client.Result;

public class ClientTest {
	
	@Test
	public void testProcessChoice() {
		//arrange
		ClientUploaderInterface uploader = EasyMock.createMock(ClientUploaderInterface.class);
		Client client = new Client(uploader, new ClientTUI());
		// --> set expectations (part of arrange)
		uploader.uploadFile();
		EasyMock.replay(uploader);
		
		//act
		assertEquals(Result.UPLOAD_STARTED, client.processChoice("u"));
		
		//assert
		EasyMock.verify(uploader);
	}
}
