package main;

import java.io.*;
import java.net.ServerSocket;

public class Server {

    private int N, M, T, port;
    private String fileName, serverName, sid, userName, password;

    private int successful, failed;

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

    public Long[] runThreads() {
        long start = System.currentTimeMillis();
        try {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Waiting...");
                for (int i = 0; i < N; i++) {
                    Runnable r = new ThreadHandler(this, M, fileName, serverName, port, sid, userName, password);
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
        long finish = System.currentTimeMillis();
        long timeConsumedMillis = finish - start;
        Long[] result = new Long[3];
        result[0] = timeConsumedMillis;
        result[1] = (long) successful;
        result[2] = (long) failed;
        return result;
    }

    void setResult(boolean res) {
        if (res)
            successful++;
        else
            failed++;
    }
}