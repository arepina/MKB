package main.webapp;

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
        String N = request.getParameter("N");
        String M = request.getParameter("M");
        String file = request.getParameter("fileName");
        Double T = Double.parseDouble(request.getParameter("T"));
        String server = request.getParameter("server");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.getWriter().println("<html>");
        response.getWriter().println("<head>");
        response.getWriter().println("<title>Job done</title>");
        response.getWriter().println("</head>");
        response.getWriter().println("<body>");
        response.getWriter().println("Go to logs");
        response.getWriter().println("</body>");
        response.getWriter().println("</html>");
    }
}
