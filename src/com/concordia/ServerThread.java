package com.concordia;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

public class ServerThread extends Thread{

    private final Socket SOCKET;

    public ServerThread(Socket socket) {
        this.SOCKET = socket;
    }

    @Override
    public void run() {
        try {
            // create buffered input/output
            BufferedReader in = new BufferedReader(new InputStreamReader(this.SOCKET.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.SOCKET.getOutputStream()));
            // print the request in the console
            String inputString;
            boolean isFirstLine = true;
            String[] firstLine = {};
            HashMap<String, String> headers = new HashMap<>();
            // initialize an error string to return if there is in error
            String[] error = {"", ""};

            while ((inputString = in.readLine()) != null) {
                // break out of the loop when an empty line is reached
                if (inputString.isEmpty()) {
                    break;
                }
                // if it's the first line, parse it into a string array
                // firstLine = [method, path, protocol]
                else if (isFirstLine) {
                    firstLine = inputString.split(" ");
                    isFirstLine = false;
                }
                // if it's not first line, it's the headers
                // store the headers in a hashmap
                else {
                    // read headers
                    String[] header = inputString.split(":");
                    // trim the empty spaces left by the split
                    headers.put(header[0].trim(), header[1].trim());
                }
                // output the request to the console
                System.out.println(inputString);
            }

            // create a StringBuilder for the body
            StringBuilder body = new StringBuilder();
            // only attempt reading the body if the method is POST
            // and the headers contain the Content-Length header
            // and the Content-Type header
            if (firstLine[0].equalsIgnoreCase("POST") &&
                    headers.containsKey("Content-Length") &&
                    headers.containsKey("Content-Type")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                for (int i = 0; i < contentLength; i++) {
                    body.append((char) in.read());
                }
            } else if (firstLine[0].equalsIgnoreCase("POST") &&
                    (!headers.containsKey("Content-Type") || !headers.containsKey("Content-Length"))) {
                error[0] = "400";
                error[1] = "POST request is missing Content-Type or Content-Length header.\n";
            }
            // print the body to the console
            System.out.println("\n" + body);
            // process the request and return the response
            out.write(Server.processRequest(firstLine, headers, body.toString(), error));

            // close the connection
            out.close();
            in.close();
            this.SOCKET.close();
        } catch (IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
    }
}
