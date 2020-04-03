package com.nedap.university.eline.exchanger.server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This program demonstrates how to implement a UDP server program.
 *
 *
 * @author www.codejava.net
 */
public class Server {
    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
 
    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
 
    public static void main(String[] args) {
        int port = 8080;
 
        try {
            Server server = new Server(port);
            server.setUpUDPConnection();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException e) {
        	System.out.println("IOexception: " + e.getMessage());
		}
    }
    
    private void setUpUDPConnection() throws IOException {
    	//TODO actually don't think I need this, I can get this info each time a packet is sent
	    DatagramPacket request = new DatagramPacket(new byte[1], 1);
	    socket.receive(request);
 
        clientAddress = request.getAddress();
        clientPort = request.getPort();
    }
}

