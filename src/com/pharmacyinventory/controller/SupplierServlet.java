package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.SupplierDao;
import com.pharmacyinventory.model.Supplier;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@WebServlet("/suppliers")
public class SupplierServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final SupplierDao supplierDao = new SupplierDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        try {
            if ("delete".equalsIgnoreCase(action) && idParam != null) {
                long id = Long.parseLong(idParam);
                supplierDao.delete(id);
                response.sendRedirect(request.getContextPath() + "/suppliers?success="
                        + encode("Supplier deleted successfully."));
                return;
            }

            if ("edit".equalsIgnoreCase(action) && idParam != null) {
                long id = Long.parseLong(idParam);
                Optional<Supplier> supplier = supplierDao.findById(id);
                supplier.ifPresent(value -> request.setAttribute("editSupplier", value));
            }

            request.setAttribute("suppliers", supplierDao.findAll());
            request.getRequestDispatcher("/views/suppliers.jsp").forward(request, response);
        } catch (NumberFormatException | SQLException e) {
            request.setAttribute("error", "Unable to process supplier request.");
            forwardWithList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        String idParam = trim(request.getParameter("id"));
        String supplierCode = trim(request.getParameter("supplierCode"));
        String name = trim(request.getParameter("name"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email"));
        String gstNo = trim(request.getParameter("gstNo"));
        String addressLine = trim(request.getParameter("addressLine"));

        if (isBlank(supplierCode) || isBlank(name)) {
            request.setAttribute("error", "Supplier code and supplier name are required.");
            request.setAttribute("editSupplier", buildSupplierFromInput(idParam, supplierCode, name, phone, email, gstNo, addressLine));
            forwardWithList(request, response);
            return;
        }

        if (!isBlank(idParam) && parseLongOrDefault(idParam, -1) <= 0) {
            request.setAttribute("error", "Invalid supplier ID.");
            request.setAttribute("editSupplier", buildSupplierFromInput("", supplierCode, name, phone, email, gstNo, addressLine));
            forwardWithList(request, response);
            return;
        }

        try {
            Supplier supplier = buildSupplierFromInput(idParam, supplierCode, name, phone, email, gstNo, addressLine);
            boolean saved;
            if (isBlank(idParam)) {
                saved = supplierDao.create(supplier);
            } else {
                saved = supplierDao.update(supplier);
            }

            if (!saved) {
                request.setAttribute("error", "No changes were saved. Please try again.");
                request.setAttribute("editSupplier", supplier);
                forwardWithList(request, response);
                return;
            }

            if (isBlank(idParam)) {
                response.sendRedirect(request.getContextPath() + "/suppliers?success="
                        + encode("Supplier added successfully."));
            } else {
                response.sendRedirect(request.getContextPath() + "/suppliers?success="
                        + encode("Supplier updated successfully."));
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to save supplier. Code may already exist.");
            request.setAttribute("editSupplier", buildSupplierFromInput(idParam, supplierCode, name, phone, email, gstNo, addressLine));
            forwardWithList(request, response);
        }
    }

    private Supplier buildSupplierFromInput(String idParam, String supplierCode, String name, String phone,
                                            String email, String gstNo, String addressLine) {
        Supplier supplier = new Supplier();
        if (!isBlank(idParam)) {
            supplier.setId(parseLongOrDefault(idParam, 0));
        }
        supplier.setSupplierCode(supplierCode);
        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setGstNo(gstNo);
        supplier.setAddressLine(addressLine);
        return supplier;
    }

    private void forwardWithList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("suppliers", supplierDao.findAll());
        } catch (SQLException e) {
            request.setAttribute("suppliers", Collections.emptyList());
        }
        request.getRequestDispatcher("/views/suppliers.jsp").forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private long parseLongOrDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
