package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.*;
import java.net.Socket;

public class Dispatcher implements Runnable {

    private Socket clientSocket;
    private BufferedReader inputBufferedReader;
    private DataOutputStream outputStream;


    Dispatcher(Socket clientSocket) {

        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        StringBuilder requestBuilder = new StringBuilder();

        String line = null;


        // handle client connection
        System.out.println("INFO: Client accepted: " + clientSocket);
        setupStreams();

        try {
            while(clientSocket.isBound()) {
                // read line from socket input reader
                line = inputBufferedReader.readLine();
                    if (line != null) {

                        if (line.isEmpty()) {

                            parseRequestHeaders(requestBuilder.toString());
                            System.out.println("INFO: Processed request.");
                            requestBuilder.delete(0, requestBuilder.length());

                            close();

                        } else {
                            requestBuilder.append(line);
                            requestBuilder.append("\r\n");
                            System.out.println("INFO: Read request line.");
                        }
                    } else {
                        requestBuilder.delete(0, requestBuilder.length());
                        close();
                    }
            }
        } catch (IOException e) {

            System.err.println(e.getMessage());
        }

    }

    private String buildResponseHeaders (File resource, byte[] content, StatusCode statusCode) throws IOException {

        StringBuilder headers = new StringBuilder();

        headers.append("HTTP/1.1 " + statusCode.getCode() + statusCode + "\r\n");
        headers.append("Content-Type: " + getFileMimeType(resource) + ((getFileType(resource).equals("text")) ? ";charset=UTF-8 \r\n" : "\r\n"));
        headers.append("Content-Length: " + content.length + "\r\n");
        headers.append("\r\n");

        return headers.toString();
    }

    private String getFileMimeType(File resource) {

        String [] resource_split = resource.getName().split("/");
        String fileName = resource_split[resource_split.length - 1];
        String extension = fileName.split("\\.")[1];

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpg";
            case "png":
                return "image/png";
            case "txt":
                return "text/plain";
            case "htm":
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }

    /**
     *
     * @return The file type (text or binary) of the file referenced by the property resource of the type File.
     */
    private String getFileType(File resource) {

        switch (getFileMimeType(resource).split("/")[0]) {

            case "text":
                return "text";
            default:
                return "binary";
        }
    }

    private boolean isValidFile (File file) {

        return file.exists() && file.isFile() && file.canRead();
    }

    private byte[] readResource(File resource) {

        BufferedInputStream fileInput;
        byte[] fileToBytes = new byte[(int) resource.length()];      // Dangerous, may result in program breakage if a file too big is specified in the resource url path
        int bytesRead = 0;

        if (!isValidFile(resource)) {
            System.err.println("ERROR: Resource specified by url path provided in the request not able to be read.");
        }

        try {
            fileInput = new BufferedInputStream(new FileInputStream(resource.getPath()));
            bytesRead = fileInput.read(fileToBytes, 0, fileToBytes.length);
            System.out.println("INFO: " + bytesRead + " bytes read");

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return fileToBytes;
    }

    /**
     * Instantiate a buffered reader from the input stream of client socket
     */

    private void setupStreams() {

        try {
            inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), ConcurrentWebServer.CHARSET));
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

    }

    private void sendResponse (File resource, StatusCode statusCode) throws IOException {

        byte[] content = readResource(resource);

        String responseHeaders = buildResponseHeaders(resource, content, statusCode);

        byte[] responseInBytes = responseHeaders.getBytes(ConcurrentWebServer.CHARSET);

        int oldBytesWritten = outputStream.size();

        outputStream.write(responseInBytes);
        System.out.println("INFO: Uploading resource: " + resource.getName());
        outputStream.write(content, 0, content.length);
        System.out.println("INFO: " + (outputStream.size() - oldBytesWritten) + " bytes written.");
        outputStream.flush();
    }

    private void parseRequestHeaders (String request) throws IOException {

        String[] requestLines = request.split("(\r|\n)");
        String[] requestLine = requestLines[0].split(" ");
        String httpVerb;
        String resourcePath;
        String httpVersion;
        String response = null;
        File resource = null;

        // Currently ignoring all other headers other than the initial request line
        // Only responding to a GET Request

        if (requestLine.length == 3) {
            httpVerb = requestLine[0];
            resourcePath = requestLine[1];
            httpVersion = requestLine[2];

            // prepend www to every path from the URL
            resourcePath = resourcePath.substring(1);
            //resourcePath = "www/" + resourcePath;

            resource = new File(resourcePath);

            switch (httpVerb) {

                case "GET":
                    if (!isValidFile(resource)) {
                        System.err.println("ERROR: Invalid file. Returning 404 page.");
                        sendResponse(ConcurrentWebServer.NOT_FOUND_HTML_FILE, StatusCode.NOT_FOUND);
                    } else {
                        sendResponse(resource, StatusCode.OK);
                    }
                    break;
                default:
                    System.err.println("ERROR: HTTP Request not yet implemented!");
                    sendResponse(ConcurrentWebServer.NOT_IMPLEMENTED_FILE, StatusCode.NOT_IMPLEMENTED);
            }
        }
    }

    /**
     * Closes itself
     */

    private void close () {

        try {

            if (clientSocket != null) {
                System.out.println("INFO: Closing client connection");
                clientSocket.close();
            }

        } catch (IOException e) {
            System.out.println("ERROR: Error closing connection " + e.getMessage());
        }
    }
}
