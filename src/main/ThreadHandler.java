package main;


import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jdk.nashorn.internal.parser.TokenType.EOL;

public class ThreadHandler implements Runnable {

    private int M, port;
    private String fileName, serverName, dbname, userName, password;
    private Server server;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    ThreadHandler(Server server, int M, String fileName, String serverName, int port, String dbname, String userName, String password) {
        this.server = server;
        this.M = M;
        this.fileName = fileName;
        this.serverName = serverName;
        this.port = port;
        this.dbname = dbname;
        this.userName = userName;
        this.password = password;
    }

    private String readFile() {
        StringBuilder result = new StringBuilder("");
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            try (Scanner scanner = new Scanner(file)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line.replace("\\n\\n", ""));
                }
                scanner.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
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
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("MySql JDBC Driver is not found");
            e.printStackTrace();
            return false;
        }
        String url = String.format("jdbc:mysql://%s:%d/%s", serverName, port, dbname);

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
            List<String> parsedCommands = Arrays.asList(allCommands.split(";"));
            for (int i = 0; i < M; i++) // execute the queries M times
            {
                for (String query : parsedCommands) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("SQLException on operations execution");
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
        if (!connect()) {
            server.setResult(false);
            throw new IllegalStateException("There was an error during jdbc connection in one of the threads");
        }
        server.setResult(true);
        System.out.println("Finished");
    }
}