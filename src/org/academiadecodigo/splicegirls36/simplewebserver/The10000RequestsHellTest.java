package org.academiadecodigo.splicegirls36.simplewebserver;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class The10000RequestsHellTest {

    public static void main(String[] args) {

        ExecutorService clients = Executors.newCachedThreadPool();

        for (int i = 0; i < 1000000; i++) {

            clients.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("localhost", 8080);
                        while (true) {
                            socket.getOutputStream().write(0);
                            socket.getOutputStream().flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }

}
