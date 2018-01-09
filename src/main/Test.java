package main;


public class Test {

    public static void main(String[] args)
    {
        int N = 3;
        int M = 3;
        String fileName = "test.txt";
        int T = 1000;
        String serverName = "localhost";
        int port = 1234;
        String sid = "SE";
        String userName = "a";
        String password = "123456";

        Server s = new Server(N, M, fileName, T, serverName, port, sid, userName, password);
        s.runThreads(); //run threads
    }
}
