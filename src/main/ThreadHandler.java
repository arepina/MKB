package main;


import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static jdk.nashorn.internal.parser.TokenType.EOL;

public class ThreadHandler implements Runnable {

    private int M, port;
    private String fileName, serverName, sid, userName, password;
    private Socket clientSocket;

    ThreadHandler(Socket client, int M, String fileName, String serverName, int port, String sid, String userName, String password) {
        this.M = M;
        this.fileName = fileName;
        this.serverName = serverName;
        this.port = port;
        this.sid = sid;
        this.userName = userName;
        this.password = password;
        this.clientSocket = client;
    }

    private String readFile() throws IOException {
        BufferedReader br = null;
        FileReader fr = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String nextLine = "";
            while ((nextLine = br.readLine()) != null) {
                sb.append(nextLine); // BufferedReader strips the EOL character
                // so we add a new one
                sb.append(EOL);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) br.close();
            if (fr != null) fr.close();
        }
        return sb.toString();
    }

    private boolean connect() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC Driver is not found");
            e.printStackTrace();
            return false;
        }
        String url = String.format("jdbc:oracle:thin:@%s:%d:%s", serverName, port, sid);

        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection(url, userName, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Connection Failed : " + e.getMessage());
            return false;
        }
        try {
            String allCommands = readFile();
            List<String> parsedCommands= Arrays.asList(allCommands.split("\\n/\\n"));
            for(int i = 0; i < M; i++) // execute the queries M times
            {
                for(String query : parsedCommands) {
                    boolean status = statement.execute(query);
                    if (status) {
                        // query is a select query
                        ResultSet rs = statement.getResultSet();
                        while (rs.next()) {
                            System.out.println(rs.getString(1));
                        }
                        rs.close();
                    } else {
                        // query can be update or any query apart from select query
                        int count = statement.getUpdateCount();
                        System.out.println("Total records updated: " + count);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!connect())
            throw new IllegalStateException("There was an error during jdbc connection in one of the threads");
        Server.numberOfOnline--;
        System.out.println("There are " + Server.numberOfOnline + " clients online");
    }

}