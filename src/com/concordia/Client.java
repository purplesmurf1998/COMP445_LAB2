package com.concordia;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        System.out.println("=============================================");
        System.out.println("httpfs is a simple file server. ");
        System.out.println("usage:");
        System.out.println("\thttpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        System.out.println("For default port and directory:");
        System.out.println("\thttpfs [-v]");
        System.out.println("=============================================");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] arguments = input.split(" ");

        Server server = parseInput(arguments);

        // start the server
        try {
             server.listen();
        } catch(IOException ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
    }

    private static int contains(String[] arr, String key) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(key))
                return i;
        }
        return -1;
    }

    private static Server parseInput(String[] input) {
        // parse the input and create a new Server accordingly
        boolean verbose = false; //default verbose option
        int port = 8080; //default port
        String directory = System.getProperty("user.dir") + "/serverDir";//default server directory

        // end the launch sequence if command not formatted properly
        if (!input[0].equalsIgnoreCase("httpfs")) {
            System.out.println("Start command must start with [httpfs]. Terminating launch sequence.");
            System.exit(0);
        }

        // determine if verbose argument is active
        if (contains(input, "-v") > 0) {
            verbose = true;
        }

        int portIndex = contains(input, "-p");
        int dirIndex = contains(input, "-d");

        // set the port number if it's in the arguments
        if (portIndex > 0) {
            try {
                port = Integer.parseInt(input[portIndex + 1]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port number. Terminating launch sequence.");
                System.exit(0);
            }
        }

        // set the server directory if it's in the arguments
        if (dirIndex > 0) {
            directory = input[dirIndex + 1];
        }

        // all the arguments are set, create and return the new server
        return new Server(verbose, port, directory);
    }
}
