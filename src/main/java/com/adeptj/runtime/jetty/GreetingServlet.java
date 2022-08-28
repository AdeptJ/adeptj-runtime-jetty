package com.adeptj.runtime.jetty;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(
        name = "GreetingServlet",
        urlPatterns = {"/greet"}
)
public class GreetingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter writer = resp.getWriter();
        writer.println("<html><title>Welcome</title><body>");
        writer.println("<h1>Have a nice day!</h1>");
        writer.println("</body></html>");
    }
}
