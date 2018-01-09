package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Server {

    private int N, M, T, port;
    private String fileName, serverName, sid, userName, password;

    static int numberOfOnline; //active clients number

    public Server(int N, int M, String fileName, int T, String serverName, int port, String sid, String username, String password) {
        this.N = N;
        this.M = M;
        this.T = T;
        this.fileName = fileName;
        this.serverName = serverName;
        this.port = port;
        this.sid = sid;
        this.userName = username;
        this.password = password;
    }

    public void runThreads() {
        try {
            try (ServerSocket server = new ServerSocket(port)) {
                Socket client = server.accept();
                System.out.println("Waiting...");
                numberOfOnline = 0;
                // wait for clients, create new thread for each
                for (int i = 0; i < N; i++) {
                    numberOfOnline++;
                    System.out.println("There are " + Server.numberOfOnline + " clients online");
                    Runnable r = new ThreadHandler(client, M, fileName, serverName, port, sid, userName, password);
                    Thread t = new Thread(r);
                    t.start();
                    Thread.sleep(T); // pause between threads
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}