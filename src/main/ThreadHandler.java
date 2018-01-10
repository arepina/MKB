package main;


import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jdk.nashorn.internal.parser.TokenType.EOL;

public class ThreadHandler implements Runnable {

    private int M, port;
    private String fileName, serverName, sid, userName, password;
    private Server server;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    ThreadHandler(Server server, int M, String fileName, String serverName, int port, String sid, String userName, String password) {
        this.server = server;
        this.M = M;
        this.fileName = fileName;
        this.serverName = serverName;
        this.port = port;
        this.sid = sid;
        this.userName = userName;
        this.password = password;
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
            Logs.setup();
            LOGGER.setLevel(Level.INFO);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problems with creating the log files");
        }
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Oracle JDBC Driver is not found");
            e.printStackTrace();
            return false;
        }
        String url = String.format("jdbc:oracle:thin:@%s:%d:%s", serverName, port, sid);

        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection(url, userName, password);
            LOGGER.info("Connection succeed");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Connection Failed");
            return false;
        }
        try {
            String allCommands = readFile();
            List<String> parsedCommands= Arrays.asList(allCommands.split("\\n/\\n"));
            for(int i = 0; i < M; i++) // execute the queries M times
            {
                for(String query : parsedCommands) {
                    boolean status = statement.execute(query);
                    LOGGER.info("Iteration:" + i + " Status: " + status);
                    if (status) {
                        // query is a select query
                        ResultSet rs = statement.getResultSet();
                        while (rs.next()) {
                            LOGGER.info(rs.getString(1));
                        }
                        rs.close();
                    } else {
                        // query can be update or any query apart from select query
                        int count = statement.getUpdateCount();
                        LOGGER.info("Total records updated: " + count);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            LOGGER.severe("IOException | SQLException");
            return false;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("SQLException on connection close");
            return false;
        }
        LOGGER.info("Successful");
        return true;
    }

    public void run() {
        System.out.println("Started");
        if(!connect()) {
            server.setResult(false);
            throw new IllegalStateException("There was an error during jdbc connection in one of the threads");
        }
        server.setResult(true);
    }
}