package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.UserDao;
import com.pharmacyinventory.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher("/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isBlank(username) || isBlank(password)) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
            return;
        }

        try {
            Optional<User> userOptional = userDao.authenticate(username.trim(), password);
            if (userOptional.isEmpty()) {
                request.setAttribute("error", "Invalid username or password.");
                request.getRequestDispatcher("/views/login.jsp").forward(request, response);
                return;
            }

            User user = userOptional.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedInUser", user.getUsername());
            session.setAttribute("loggedInUserId", user.getId());
            session.setAttribute("loggedInUserName", user.getFullName());
            session.setAttribute("loggedInUserRole", user.getRole());
            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to log in right now. Please try again.");
            request.getRequestDispatcher("/views/login.jsp").forward(request, response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
