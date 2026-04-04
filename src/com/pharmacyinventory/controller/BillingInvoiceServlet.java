package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.BillingDao;
import com.pharmacyinventory.model.SaleInvoiceView;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/billing/invoice")
public class BillingInvoiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final BillingDao billingDao = new BillingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        String saleIdRaw = request.getParameter("saleId");
        long saleId;
        try {
            saleId = Long.parseLong(saleIdRaw);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/billing?error=Invalid%20invoice%20reference.");
            return;
        }

        try {
            Optional<SaleInvoiceView> invoice = billingDao.findInvoiceBySaleId(saleId);
            if (invoice.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/billing?error=Invoice%20not%20found.");
                return;
            }
            request.setAttribute("invoice", invoice.get());
            request.getRequestDispatcher("/views/billing-invoice.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/billing?error=Unable%20to%20load%20invoice.");
        }
    }
}
