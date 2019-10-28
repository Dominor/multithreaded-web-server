package org.academiadecodigo.splicegirls36.simplewebserver;

public class Main {

    public static void main(String[] args) {

        ConcurrentWebServer webServer = null;

        try {
            // try to create an instance of the ChatServer at port specified at args[0]
            if (args.length > 0) {
                webServer = new ConcurrentWebServer(Integer.parseInt(args[0]));
                webServer.start();
            }
        } catch (NumberFormatException e) {
            System.out.println("Usage: SimpleWebServer [PORT]");
            System.exit(1);
        }
    }
}
