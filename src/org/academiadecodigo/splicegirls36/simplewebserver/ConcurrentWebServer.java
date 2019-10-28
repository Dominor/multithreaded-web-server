package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ConcurrentWebServer {

    public static String CHARSET = "UTF8";

    public static final File NOT_FOUND_HTML_FILE = new File("www/404.html");
    public static final File NOT_IMPLEMENTED_FILE = new File("www/501.html");

    private ServerSocket serverSocket;

    public ConcurrentWebServer(int port) {

        try {

            //bind the socket to specified port
            System.out.println("INFO: Binding to port " + port);
            serverSocket = new ServerSocket(port);

            System.out.println("INFO: Server started: " + serverSocket);

        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            close();
            System.exit(1);
        }
    }

    public void start() {

        Socket clientSocket = null;
        Thread dispatcherThread = null;

        try {

            while (serverSocket.isBound()) {

                System.out.println(Thread.currentThread().getName());
                // block waiting for a client to connect
                System.out.println("INFO: Waiting for a client connection.");
                clientSocket = serverSocket.accept();

                dispatcherThread = new Thread(new Dispatcher(clientSocket));
                dispatcherThread.start();

            }
        } catch(IOException e){

            System.err.println("ERROR: Client connection not accepted. " + clientSocket + "Problem: " + e.getMessage());
        }
    }

    /**
     * Close itself
     */

    private void close () {

        try {

            if (serverSocket != null) {
                System.out.println("INFO: Closing server socket");
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("ERROR: Error closing connection " + e.getMessage());
        }
    }
}
