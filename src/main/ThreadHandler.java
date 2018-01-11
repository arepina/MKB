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

    private int M, port, threadNumber;
    private String fileName, serverName, dbname, userName, password;
    private Server server;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    ThreadHandler(Server server, int M, String fileName, String serverName, int port, String dbname, String userName, String password, int i) {
        this.server = server;
        this.M = M;
        this.fileName = fileName;
        this.serverName = serverName;
        this.port = port;
        this.dbname = dbname;
        this.userName = userName;
        this.password = password;
        this.threadNumber = i;
    }

    private String readFile() {
        StringBuilder result = new StringBuilder("");
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            try (Scanner scanner = new Scanner(file)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    result.append(line.replace("\\n/\\n", ""));
                }
                scanner.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Thread # " + threadNumber + ": File not found");
        }
        return result.toString();
    }

    private void executeQueries(Statement statement) throws SQLException {
        String allCommands = readFile();
        List<String> parsedCommands = Arrays.asList(allCommands.split(";"));
        for (int i = 0; i < M; i++) // execute the queries M times
        {
            int commandNum = 1;
            for (String query : parsedCommands) {
                boolean status = statement.execute(query);
                if (status) {
                    // query is a select query
                    ResultSet rs = statement.getResultSet();
                    int columns = rs.getMetaData().getColumnCount();
                    while (rs.next()) {
                        String queryRes = "";
                        for (int k = 1; k <= columns; k++)
                            queryRes += rs.getString(k) + "\t";
                        LOGGER.info("Thread # " + threadNumber + ": Iteration # " + (i + 1) + " Query # " + commandNum + " - " + queryRes);
                    }
                    rs.close();
                } else {
                    // query can be update or any query apart from select query
                    int count = statement.getUpdateCount();
                    LOGGER.info("Thread # " + threadNumber + ": Iteration # " + (i + 1) + " Query # " + commandNum + " - Total records updated: " + count);
                }
                commandNum++;
            }
        }
    }

    private boolean connect() {
        try {
            Logs.setup();
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problems with creating the log files");
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Thread # " + threadNumber + ": MySql JDBC Driver is not found");
            e.printStackTrace();
            return false;
        }
        String url = String.format("jdbc:mysql://%s:%d/%s", serverName, port, dbname);

        Connection connection;
        Statement statement;
        try {
            connection = DriverManager.getConnection(url, userName, password);
            LOGGER.info("Thread # " + threadNumber + ": Connection succeed");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Thread # " + threadNumber + ": Connection Failed");
            return false;
        }
        try {
            executeQueries(statement);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Thread # " + threadNumber + ": SQLException on queries execution");
            return false;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe("Thread # " + threadNumber + ": SQLException on connection close");
            return false;
        }
        LOGGER.info("Thread # " + threadNumber + ": Successfully finished");
        return true;
    }

    public void run() {
        if (!connect()) {
            server.setResult(false);
            throw new IllegalStateException("Thread # " + threadNumber + ": There was an error during jdbc connection in one of the threads");
        }
        server.setResult(true);
    }
}