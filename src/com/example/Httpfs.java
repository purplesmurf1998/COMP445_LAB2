package com.example;
import java.io.IOException;

public class Httpfs {

	//private int port = 8080;
	public static void main(String[] args) throws IOException {
		Server server = new Server(args);
		try
		{
				
			if (args[0].equals("help"))
				System.out.println("This is a server");					
			server.handleRequests(args);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.getMessage();
		}
	}
			
}

