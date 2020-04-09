package com.nedap.university.eline.exchanger.client;

import org.easymock.EasyMock;

import protocol.ProtocolMessages;
import server.Game;
import server.Handler;

public class ClientTest {
	
	//@Test
	public void testStart() {
		//can access the (non-public!!) ExampleClass, because in same package
		Client client = new Client();
		
		
		//arrange
		Game game1 = new Game(1, "1.0", 5, 100);
		Handler handler1 = EasyMock.createMock(Handler.class);
		Handler handler2 = EasyMock.createMock(Handler.class);
		game1.setClientHandlerPlayer1(handler1);
		game1.setClientHandlerPlayer2(handler2);
		game1.setColorPlayer1(ProtocolMessages.BLACK);
		game1.setColorPlayer2(ProtocolMessages.WHITE);
		
		Game game2 = new Game(2, "1.0", 5, 100);
		Handler handler3 = EasyMock.createMock(Handler.class);
		Handler handler4 = EasyMock.createMock(Handler.class);
		game2.setClientHandlerPlayer1(handler3);
		game2.setClientHandlerPlayer2(handler4);
		game2.setColorPlayer1(ProtocolMessages.WHITE);
		game2.setColorPlayer2(ProtocolMessages.BLACK);
		
		// --> set expectations
		handler2.sendMessageToClient("G;UUUUUUUUUUUUUUUUUUUUUUUUU;W");
		handler1.sendMessageToClient("T;UUUUUUUUUUUUUUUUUUUUUUUUU;null");
		EasyMock.expect(handler1.getReply()).andReturn("M;0");
		handler1.sendMessageToClient("R;V;BUUUUUUUUUUUUUUUUUUUUUUUU");

		handler4.sendMessageToClient("G;UUUUUUUUUUUUUUUUUUUUUUUUU;B");
		handler4.sendMessageToClient("T;UUUUUUUUUUUUUUUUUUUUUUUUU;null");
		EasyMock.expect(handler4.getReply()).andReturn("M;0");
		handler4.sendMessageToClient("R;V;BUUUUUUUUUUUUUUUUUUUUUUUU");
		
		EasyMock.replay(handler1, handler2, handler3, handler4);
		
		//act
		client.start();
		
		//assert
		EasyMock.verify(handler1, handler2, handler3, handler4);

	}
}
