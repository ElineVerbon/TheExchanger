//package com.nedap.university.eline.exchanger.client;
//
//import org.easymock.EasyMock;
//import org.junit.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import com.nedap.university.eline.exchanger.communication.CommunicationMessages;
//
//public class ClientTest {
//	
//	@Test
//	public void testProcessChoice() {
//		//arrange
//		ClientUploaderInterface uploader = EasyMock.createMock(ClientUploaderInterface.class);
//		
//		Client client = new Client(uploader, listAsker);
//		// --> set expectations (part of arrange)
//		uploader.letClientUploadFile();
//		EasyMock.replay(uploader);
//		
//		//act
//		assertEquals(CommunicationMessages.UPLOAD, client.processChoice("u"));
//		
//		//assert
//		EasyMock.verify(uploader);
//	}
//}
