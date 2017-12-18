package main;

import server.GoBangServer;

public class Server {
	
	public static void main(String[] args) {
		new GoBangServer(13140).start();
	}
	
}
