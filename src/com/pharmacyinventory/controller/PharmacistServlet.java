package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.UserDao;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@WebServlet("/pharmacists")
public class PharmacistServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireAdmin(request, response)) {
            return;
        }

        try {
            request.setAttribute("pharmacists", userDao.findPharmacists());
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load pharmacist users right now.");
        }
        request.getRequestDispatcher("/views/pharmacists.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.requireAdmin(request, response)) {
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");

        if (fullName.isEmpty() || username.isEmpty() || password == null || password.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/pharmacists?error="
                    + encode("Full name, username and password are required."));
            return;
        }
        if (password.length() < 6) {
            response.sendRedirect(request.getContextPath() + "/pharmacists?error="
                    + encode("Password must be at least 6 characters."));
            return;
        }

        try {
            if (userDao.usernameExists(username)) {
                response.sendRedirect(request.getContextPath() + "/pharmacists?error="
                        + encode("Username already exists. Choose another username."));
                return;
            }
            boolean created = userDao.createPharmacist(fullName, username, password);
            if (!created) {
                response.sendRedirect(request.getContextPath() + "/pharmacists?error="
                        + encode("Unable to create pharmacist right now."));
                return;
            }
            response.sendRedirect(request.getContextPath() + "/pharmacists?success="
                    + encode("Pharmacist account created successfully."));
        } catch (SQLException e) {
            response.sendRedirect(request.getContextPath() + "/pharmacists?error="
                    + encode("Database error while creating pharmacist."));
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
