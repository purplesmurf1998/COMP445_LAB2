package com.company;

import java.io.IOException;

public class Client {

    public static void main(String[] args) {
	    // write your code here
        Server server = new Server();

        // start the server
        try {
             server.listen();
        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
            System.err.println(ex.getMessage());
        }
    }
}
