package main;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class Server {
    public Server(int n, int m, File file, int T, String serverName, String username, String password) {
        try {
            Socket client = null;
            try (ServerSocket server = new ServerSocket(8080)) {
                System.out.println("Waiting...");
                numberOfOnline = 0;
                // wait for clients, create new thread for each
                while (true) {
                    client = server.accept(); // wait for connection
                    numberOfOnline++;
                    System.out.println("One more client has been connected");
                    System.out.println("There are " + Server.numberOfOnline + " clients online");
                    Runnable r = new ThreadEchoHandler(client);
                    Thread t = new Thread(r);
                    t.start();
                }
            } finally {
                if (client != null) {
                    client.close();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int numberOfOnline; //active clients number
}


class ThreadEchoHandler implements Runnable {
    ThreadEchoHandler(Socket st) {
        client = st;
    }

    public void run() {
        try {
            // create server's input stream
            InputStream inStream = client.getInputStream();
            BufferedReader inputLine = new BufferedReader(new InputStreamReader(inStream));
            String stringFromClient = inputLine.readLine(); //Строка, полученная от клиента
            System.out.println(stringFromClient);

            // create server's output stream
            OutputStream outStream = client.getOutputStream();
            PrintWriter out = new PrintWriter(outStream, true);
            // get the webpage data and send response to client
            try {
                URL url = new URL(stringFromClient);
                LineNumberReader lineReader = new LineNumberReader(new
                        InputStreamReader(url.openStream()));

                String s = lineReader.readLine(); // read page
                // send lines to client
                while (s != null) {
                    out.println(s);
                    s = lineReader.readLine();
                }

                lineReader.close();
            } catch (MalformedURLException e) {
                out.println("Malformed URL");
            } catch (IOException e) {
                out.println("Probably this page does not exist");
            }
            client.close();
            Server.numberOfOnline--;
            System.out.println("There are " + Server.numberOfOnline + " clients online");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Socket client;
}

