package com.concordia;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Server {
    private static boolean verbose = false;
    private static int port = 8080; // default port
    private static String directory = System.getProperty("user.dir") + "/serverDir"; // default directory

    // constructor that uses default values for port and directory
    // used for testing purposes
    public Server() {
        System.out.println("Created server with following properties:\n" +
                "\tVERBOSE: " + verbose + "\n" +
                "\tPORT: " + port + "\n" +
                "\tDIR: " + directory);
    }
    // constructor with custom valus for verbose, port and directory
    public Server(boolean verboseIn, int portIn, String directoryIn) {
        verbose = verboseIn;
        port = portIn;
        directory = directoryIn;

        System.out.println("Created server with following properties:\n" +
                "\tVERBOSE: " + verbose + "\n" +
                "\tPORT: " + port + "\n" +
                "\tDIR: " + directory);
    }

    // start the server and start listening for requests
    public void listen() throws IOException {
        // create server socket
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on http://localhost:" + port);

        // continuously listen for connections
        while (true) {
            // wait until someone is connected
            Socket socket = serverSocket.accept();
            // when someone connects, create a new thread with the socket
            // and start it, go back to waiting for a new connection
            ServerThread serverThread = new ServerThread(socket);
            serverThread.start();
        }
    }

    // synchronize the method so only one thread can access it at a time
    // this makes sure no two processes are trying to read/write at the same time
    // therefore protecting the shared resources.
    protected synchronized static String processRequest(String[] args, HashMap<String, String> reqHeaders,String reqBody, String[] error) {
        // first check if there is an error
        if (!error[0].equals("")) {
            // if there is a number stored in the first position, this means there is an error
            return buildResponse(Integer.parseInt(error[0]), "text/html", error[1]);
        }

        // clean the directory path
        String dir = cleanDir(args[1]);
        // process the request
        if (dir.equals("/")) {
            // can only process GET request at this directory.
            // if not GET, return an error 400 Bad Request response
            if (args[0].equalsIgnoreCase("GET")) {
                // return the list of files in the root directory
                String[] pathNames;
                File f = new File(directory);
                pathNames = f.list();
                // append each file / pathName to the body
                String body = "";
                for (String pathName: pathNames) {
                    body += "-" + pathName + "\n";
                }
                // return the response
                return buildResponse(200, "text/html", body);
            } else {
                String body = "Can only perform a GET request at root directory.\n";
                // return the response
                return buildResponse(400, "text/html", body);
            }
        } else {
            // process GET or POST request at the given directory
            switch (args[0].toUpperCase()) {
                case "GET" -> {
                    // return the contents of the specified file
                    try {
                        // try converting file into an InputStream
                        File f = new File(directory + dir);
                        InputStream in = new FileInputStream(f);
                        // build the body by reading the file
                        String body = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n")) + "\n";
                        // check the request headers if the file is to be downloaded
                        boolean download = reqHeaders.getOrDefault("Download", "false").equalsIgnoreCase("true");
                        // close input stream before returning
                        in.close();
                        if (!download)
                            return buildResponse(200, reqHeaders.getOrDefault("Accept", "text/html"), body);
                        else
                            return buildDownloadResponse(200, reqHeaders.getOrDefault("Accept", "text/html"), body, f.getName());
                    } catch (FileNotFoundException ex) {
                        // return a 404 Not Found response
                        String body = "File not found in the directory.\n";
                        // return the response
                        return buildResponse(404, "text/html", body);
                    } catch (IOException ex) {
                        // return a 500 Internal Server Error response
                        String body = "Error thrown when trying to close input stream.\n";
                        // return the response
                        return buildResponse(500, "text/html", body);
                    }

                }
                case "POST" -> {
                    try {
                        File f = new File(directory + dir);
                        boolean overwrite = reqHeaders.getOrDefault("Overwrite", "true").equalsIgnoreCase("true");
                        // flip overwrite since FileWriter asks to append or not
                        FileWriter fileWriter = new FileWriter(f, !overwrite);
                        fileWriter.write(reqBody);
                        fileWriter.close();

                        String body = "POST request successful\n";
                        // return the response
                        return buildResponse(200, "text/html", body);
                    } catch (IOException ex) {
                        String body = "I/O Exception while trying to write to file.\n";
                        // return the response
                        return buildResponse(500, "text/html", body);
                    }
                }
                default -> {
                    String body = "Can only perform GET or POST requests\n";
                    // return the response
                    return buildResponse(400, "text/html", body);
                }
            }
        }
    }

    private static String buildResponse(int status, String contentType, String body) {
        String endLine = "\r\n";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date timestamp = calendar.getTime();
        calendar.add(Calendar.SECOND, 30);
        Date expires = calendar.getTime();

        String response = "";
        // add the request status
        if (verbose) {
            switch (status) {
                case 200 -> response += "HTTP/1.0 " + status + " OK" + endLine;
                case 400 -> response += "HTTP/1.0 " + status + " Bad Request" + endLine;
                case 404 -> response += "HTTP/1.0 " + status + " Not Found" + endLine;
                default -> response += "HTTP/1.0 " + status + " Internal Server Error" + endLine;
            }
        }
        // add the headers
        response += "Date: " + timestamp + endLine;
        response += "Expires: " + expires + endLine;
        response += "Content-Type: " + contentType + endLine;
        response += "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + endLine;
        // end the headers section
        response += endLine;
        // add the body
        response += body;

        return response;
    }

    private static String buildDownloadResponse(int status, String contentType, String body, String fileName) {
        String endLine = "\r\n";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date timestamp = calendar.getTime();
        calendar.add(Calendar.SECOND, 30);
        Date expires = calendar.getTime();

        String response = "";
        // add the request status
        if (verbose) {
            switch (status) {
                case 200 -> response += "HTTP/1.0 " + status + " OK" + endLine;
                case 400 -> response += "HTTP/1.0 " + status + " Bad Request" + endLine;
                case 404 -> response += "HTTP/1.0 " + status + " Not Found" + endLine;
                default -> response += "HTTP/1.0 " + status + " Internal Server Error" + endLine;
            }
        }
        // add the headers
        response += "Date: " + timestamp + endLine;
        response += "Expires: " + expires + endLine;
        response += "Content-Type: " + contentType + endLine;
        response += "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + endLine;
        response += "Content-Disposition: attachment; filename=\"" + fileName + "\"" + endLine;
        // end the headers section
        response += endLine;
        // add the body
        response += body;

        return response;
    }

    private static String cleanDir(String dir) {
        // if directory is the root directory, don't touch anything
        if (dir.equalsIgnoreCase("/"))
            return dir;
        // remove and command that could go outside the root directory
        return dir.replace("../", "");
    }
}
