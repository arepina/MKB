package main;


public class Test {

    public static void main(String[] args)
    {
        int N = 3;
        int M = 3;
        String fileName = "test.txt";
        int T = 1000;
        String serverName = "localhost";
        int port = 1521;
        String sid = "XE";
        String userName = "admin";
        String password = "admin";

        Server s = new Server(N, M, fileName, T, serverName, port, sid, userName, password);
        s.runThreads(); //run threads
    }
}