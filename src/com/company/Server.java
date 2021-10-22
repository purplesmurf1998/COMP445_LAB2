package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Server {
    private int port = 8080; // default port
    private String directory = System.getProperty("user.dir") + "/serverDir"; // default directory

    // constructor that uses default values for port and directory
    public Server() {
        System.out.println("Created server with following properties:\n" +
                "\tPORT: " + this.port + "\n" +
                "\tDIR: " + this.directory);
    }
    // constructor that specifies the port
    public Server(int port) {
        this.port = port;
        System.out.println("Created server with following properties:\n" +
                "\tPORT: " + this.port + "\n" +
                "\tDIR: " + this.directory);
    }
    // constructor that specifies the directory
    public Server(String directory) {
        this.directory = directory;
        System.out.println("Created server with following properties:\n" +
                "\tPORT: " + this.port + "\n" +
                "\tDIR: " + this.directory);
    }
    // constructor that specifies the port and the directory
    public Server(int port, String directory) {
        this.port = port;
        this.directory = directory;
        System.out.println("Created server with following properties:\n" +
                "\tPORT: " + this.port + "\n" +
                "\tDIR: " + this.directory);
    }

    // start the server and start listening for requests
    public void listen() throws IOException {
        // create server socket
        ServerSocket serverSocket = new ServerSocket(this.port);
        System.out.println("Server listening on http://localhost:" + this.port);

        // continuously listen for connections
        while (true) {
            // wait until someone is connected
            Socket socket = serverSocket.accept();

            // create buffered input/output
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // print the request in the console
            String inputString;
            boolean isFirstLine = true;
            String firstLine[] = {};
            HashMap<String, String> headers = new HashMap<>();

            while ((inputString = in.readLine()) != null) {
                if (inputString.isEmpty()) {
                    break;
                } else
                if (isFirstLine) {
                    firstLine = inputString.split(" ");
                    isFirstLine = false;
                } else {
                    // read headers
                    String[] header = inputString.split(":");
                    headers.put(header[0].trim(), header[1].trim());
                }
                System.out.println(inputString);
            }

            StringBuilder body = new StringBuilder();
            if (firstLine[0].equalsIgnoreCase("POST") && headers.containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                for (int i = 0; i < contentLength; i++){
                    body.append((char) in.read());
                }
            }

            System.out.println("Payload data is: "+body);
            // return the response
            out.write(processRequest(firstLine, headers, body.toString()));

            // close the connection
            out.close();
            in.close();
            socket.close();
        }
    }

    private String processRequest(String[] args, HashMap<String, String> reqHeaders,String reqBody) {
        if (args[1].equals("/")) {
            // can only process GET request at this directory.
            // if not GET, return an error 400 Bad Request response
            if (args[0].equalsIgnoreCase("GET")) {
                // return the list of files in the root directory
                String[] pathNames;
                File f = new File(this.directory);
                pathNames = f.list();

                String body = "";
                for (String pathName: pathNames) {
                    body += pathName + "\n";
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                Date timestamp = calendar.getTime();
                calendar.add(Calendar.SECOND, 30);
                Date expires = calendar.getTime();
                String[] headers = {
                        "HTTP/1.0 200 OK",
                        "Date: " + timestamp,
                        "Content-Type: text/html",
                        "Content-Length: " + body.length(),
                        "Expires: " + expires
                };

                return buildResponse(body, headers);
            } else {
                String body = "Can only perform a GET request at root directory.\n";
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                Date timestamp = calendar.getTime();
                calendar.add(Calendar.SECOND, 30);
                Date expires = calendar.getTime();
                String[] headers = {
                        "HTTP/1.0 400 Bad Request",
                        "Date: " + timestamp,
                        "Content-Type: text/html",
                        "Content-Length: " + body.length(),
                        "Expires: " + expires
                };

                return buildResponse(body, headers);
            }
        } else {
            // process GET or POST request at the given directory
            switch (args[0].toUpperCase()) {
                case "GET": {
                    // return the contents of the specified file
                    try {
                        // try converting file into inputstream
                        File f = new File(this.directory + args[1]);
                        InputStream in = new FileInputStream(f);

                        String body = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n")) + "\n";
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        Date timestamp = calendar.getTime();
                        calendar.add(Calendar.SECOND, 30);
                        Date expires = calendar.getTime();
                        String[] headers = {
                                "HTTP/1.0 200 OK",
                                "Date: " + timestamp,
                                "Content-Type: " + (reqHeaders.containsKey("Accept") ? reqHeaders.get("Accept") : "text/html"),
                                "Content-Length: " + body.length(),
                                "Expires: " + expires
                        };

                        return buildResponse(body, headers);
                    } catch (FileNotFoundException ex) {
                        // return a 404 Not Found response
                        String body = "File not found in the directory.\n";
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        Date timestamp = calendar.getTime();
                        calendar.add(Calendar.SECOND, 30);
                        Date expires = calendar.getTime();
                        String[] headers = {
                                "HTTP/1.0 404 Not Found",
                                "Date: " + timestamp,
                                "Content-Type: text/html",
                                "Content-Length: " + body.length(),
                                "Expires: " + expires
                        };

                        return buildResponse(body, headers);
                    }

                }
                case "POST": {
                    try {
                        File f = new File(this.directory + args[1]);
                        FileWriter fileWriter = new FileWriter(f, (reqHeaders.containsKey("Overwrite") ? !reqHeaders.get("Overwrite").equalsIgnoreCase("true") : false));
                        fileWriter.write(reqBody);
                        fileWriter.close();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        Date timestamp = calendar.getTime();
                        calendar.add(Calendar.SECOND, 30);
                        Date expires = calendar.getTime();
                        String[] headers = {
                                "HTTP/1.0 200 OK",
                                "Date: " + timestamp,
                                "Content-Type: " + (reqHeaders.containsKey("Content-Type") ? reqHeaders.get("Content-Type") : "text/html"),
                                "Content-Length: " + reqBody.length(),
                                "Expires: " + expires
                        };

                        return buildResponse(reqBody + "\n", headers);
                    } catch (IOException ex) {
                        String body = "I/O Exception while trying to write to file.";
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        Date timestamp = calendar.getTime();
                        calendar.add(Calendar.SECOND, 30);
                        Date expires = calendar.getTime();
                        String[] headers = {
                                "HTTP/1.0 500 Internal Server Error",
                                "Date: " + timestamp,
                                "Content-Type: text/html",
                                "Content-Length: " + body.length(),
                                "Expires: " + expires
                        };

                        return buildResponse(body, headers);
                    }
                }
                default: {
                    String body = "Can only perform GET or POST requests\n";
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    Date timestamp = calendar.getTime();
                    calendar.add(Calendar.SECOND, 30);
                    Date expires = calendar.getTime();
                    String[] headers = {
                            "HTTP/1.0 400 Bad Request",
                            "Date: " + timestamp,
                            "Content-Type: text/html",
                            "Content-Length: " + body.length(),
                            "Expires: " + expires
                    };
                    // return the response
                    return buildResponse(body, headers);
                }
            }
        }
    }

    private String buildResponse(String body, String[] headers) {
        String response = "";
        for (String header: headers) {
            response += header + "\r\n";
        }
        response += "\r\n";
        response += body;
        return response;
    }
}
