package main;


public class Test {

    public static void main(String[] args)
    {
        int N = 3;
        int M = 3;
        String fileName = "test.sql";
        int T = 1000;
        String serverName = "localhost";
        int port = 3306;
        String dbname = "MKB";
        String userName = "root";
        String password = "root";

        Server s = new Server(N, M, fileName, T, serverName, port, dbname, userName, password);
        s.runThreads(); //run threads
    }
}