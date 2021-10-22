package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Server {
	protected static boolean isVerbose=false;
	protected static boolean dirGiven=false;
	protected static String directory="C:\\Users\\Admin\\eclipse-workspace\\COMP445 - LAB1\\Resources";
	protected static String filePath="";
	protected String path="";
	protected String host="";
	protected String queries=""; //FORGET ABOUT QUERIES FOR NOW. IMPLEMENT IT AT THE END
	protected static int length=0;
	protected final String version = "HTTP/1.0";
	protected final static String extensions = "\r\n";
	protected static int port = 8080;
	
	public Server(String [] args) throws IOException {
		requestType(args);
	}
	
//	public void connect(String [] args) throws IOException
//	{
//		ServerSocket server = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());
//	}
//	
	public void handleRequests(String [] args) throws IOException {
		while (true) {
			ServerSocket server = new ServerSocket(port);
			//			ServerSocket server = new ServerSocket(port, 1, InetAddress.getLoopbackAddress());

			Socket client = server.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Scanner sc = new Scanner(System.in);
			if (sc.next().equals("exit()"))
				System.exit(0);
			int c = in.read();
			if (c!=-1)
			{
				while (c!=-1) {
					System.out.print((char) c);
				}
				PrintWriter out = new PrintWriter(client.getOutputStream());
				String body = "Heyyy there";
				out.print("HTTP/1.0 200 OK\r\nContent-Type-text/html\n\rContent-Length:" + body.length() +"\r\n\r\n" + body);
				out.flush();
			}
		}
	}
	 public static void requestType(String[] args) throws IOException
	 {
	        for (int i =0; i<args.length; i++)
	        {
	            if (args[i].equalsIgnoreCase("-v"))
	            {
	                isVerbose = true;
	            }
	            else if (args[i].equalsIgnoreCase("-p"))
	            {
	                String str= args[i+1];
	                int temp = Integer.parseInt(str);
	                if (temp >1023 && temp <655351)
	        			port = Integer.parseInt(str);
	            }
	            else if (args[i].equalsIgnoreCase("-d"))
	            {
	                dirGiven = true;
	                String givenPath = (args[i+1]);
	                createRootPath(givenPath);
	            }
	       }
	 }


	private static void createRootPath(String givenPath) throws IOException 
	{
		givenPath.replaceAll("/", "\\");
		directory = directory + givenPath;
		Path path = Paths.get(directory);
		Files.createDirectories(path);
	}

}
