package com.company;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        System.out.println("=============================================");
        System.out.println("httpfs is a simple file server. ");
        System.out.println("usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        System.out.println("=============================================");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] arguments = input.split(" ");

        boolean verbose = contains(arguments, "-v") > 0;
        int port = Integer.parseInt(arguments[contains(arguments, "-p") + 1]);
        String directory = arguments[contains(arguments, "-d") + 1];

        Server server = new Server();

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
}
