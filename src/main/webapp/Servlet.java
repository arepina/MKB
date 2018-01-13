package main.webapp;

import main.Server;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/servlet1")
public class Servlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int N = Integer.parseInt(request.getParameter("N"));
        int M = Integer.parseInt(request.getParameter("M"));
        String fileName = request.getParameter("fileName");
        int T = Integer.parseInt(request.getParameter("T"));
        String serverName = request.getParameter("server");
        int port = Integer.parseInt(request.getParameter("port"));
        String dbname = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        Server s = new Server(N, M, fileName, T, serverName, port, dbname, userName, password);
        Long[] result = s.runThreads(); //run threads

        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>Job done</title>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("Duration: " + result[0] + " ms");
        response.getWriter().println("<br>");
        response.getWriter().println("Successful operations: " + result[1]);
        response.getWriter().println("<br>");
        response.getWriter().println("Failed operations: " + result[2]);
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }
}
