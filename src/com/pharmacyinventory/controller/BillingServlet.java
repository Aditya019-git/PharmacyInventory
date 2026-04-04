package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.BillingDao;
import com.pharmacyinventory.model.BillableMedicine;
import com.pharmacyinventory.model.BillingCartItem;
import com.pharmacyinventory.util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@WebServlet("/billing")
public class BillingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Set<String> PAYMENT_MODES = Set.of("CASH", "CARD", "UPI", "MIXED");

    private final BillingDao billingDao = new BillingDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        try {
            request.setAttribute("medicines", billingDao.findBillableMedicines());
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to load billable medicines.");
        }

        List<BillingCartItem> cart = getCart(request.getSession(true));
        BigDecimal subTotal = calculateSubTotal(cart);
        request.setAttribute("cartItems", cart);
        request.setAttribute("subTotal", subTotal);
        request.getRequestDispatcher("/views/billing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.requireLogin(request, response)) {
            return;
        }

        String action = trim(request.getParameter("action"));
        HttpSession session = request.getSession(true);
        List<BillingCartItem> cart = getCart(session);

        try {
            switch (action) {
                case "addItem":
                    addItemToCart(request, cart);
                    response.sendRedirect(request.getContextPath() + "/billing?success=" + encode("Item added to bill."));
                    return;
                case "removeItem":
                    removeItemFromCart(request, cart);
                    response.sendRedirect(request.getContextPath() + "/billing?success=" + encode("Item removed from bill."));
                    return;
                case "clearCart":
                    cart.clear();
                    response.sendRedirect(request.getContextPath() + "/billing?success=" + encode("Billing cart cleared."));
                    return;
                case "checkout":
                    long saleId = checkout(request, session, cart);
                    response.sendRedirect(request.getContextPath() + "/billing/invoice?saleId=" + saleId);
                    return;
                default:
                    response.sendRedirect(request.getContextPath() + "/billing?error=" + encode("Unknown billing action."));
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/billing?error=" + encode(e.getMessage()));
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "Unable to process billing request." : e.getMessage();
            response.sendRedirect(request.getContextPath() + "/billing?error=" + encode(msg));
        }
    }

    private void addItemToCart(HttpServletRequest request, List<BillingCartItem> cart) throws SQLException {
        long medicineId = parseLongRequired(request.getParameter("medicineId"), "Select a medicine.");
        int qty = parseIntRequired(request.getParameter("qty"), "Quantity must be a number.");
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Optional<BillableMedicine> medicineOpt = billingDao.findBillableMedicineById(medicineId);
        if (medicineOpt.isEmpty()) {
            throw new IllegalArgumentException("Medicine not found.");
        }
        BillableMedicine medicine = medicineOpt.get();
        if (medicine.getAvailableQty() <= 0) {
            throw new IllegalArgumentException("Selected medicine has no saleable stock.");
        }
        if (medicine.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unable to determine medicine price for billing.");
        }

        BillingCartItem existing = null;
        for (BillingCartItem item : cart) {
            if (item.getMedicineId() == medicineId) {
                existing = item;
                break;
            }
        }

        int requestedTotalQty = qty + (existing == null ? 0 : existing.getQty());
        if (requestedTotalQty > medicine.getAvailableQty()) {
            throw new IllegalArgumentException("Only " + medicine.getAvailableQty() + " units available for this medicine.");
        }

        if (existing == null) {
            BillingCartItem item = new BillingCartItem();
            item.setMedicineId(medicine.getMedicineId());
            item.setMedicineCode(medicine.getMedicineCode());
            item.setMedicineName(medicine.getMedicineName());
            item.setQty(qty);
            item.setUnitPrice(medicine.getUnitPrice());
            cart.add(item);
        } else {
            existing.setQty(requestedTotalQty);
        }
    }

    private void removeItemFromCart(HttpServletRequest request, List<BillingCartItem> cart) {
        long medicineId = parseLongRequired(request.getParameter("medicineId"), "Invalid cart item.");
        Iterator<BillingCartItem> iterator = cart.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getMedicineId() == medicineId) {
                iterator.remove();
                return;
            }
        }
    }

    private long checkout(HttpServletRequest request, HttpSession session, List<BillingCartItem> cart) throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item before checkout.");
        }
        long userId = parseSessionUserId(session);
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid session. Please login again.");
        }

        String customerName = trim(request.getParameter("customerName"));
        if (customerName.isEmpty()) {
            customerName = "Walk-in Customer";
        }
        String paymentMode = trim(request.getParameter("paymentMode")).toUpperCase();
        if (!PAYMENT_MODES.contains(paymentMode)) {
            throw new IllegalArgumentException("Select a valid payment mode.");
        }

        BigDecimal discountPct = parsePercentage(request.getParameter("discountPercent"), "Discount");
        BigDecimal taxPct = parsePercentage(request.getParameter("taxPercent"), "Tax");

        long saleId = billingDao.createSaleWithFifo(
                customerName,
                paymentMode,
                discountPct,
                taxPct,
                new ArrayList<>(cart),
                userId
        );
        cart.clear();
        return saleId;
    }

    @SuppressWarnings("unchecked")
    private List<BillingCartItem> getCart(HttpSession session) {
        Object cartObj = session.getAttribute("billingCart");
        if (cartObj instanceof List) {
            return (List<BillingCartItem>) cartObj;
        }
        List<BillingCartItem> cart = new ArrayList<>();
        session.setAttribute("billingCart", cart);
        return cart;
    }

    private BigDecimal calculateSubTotal(List<BillingCartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (BillingCartItem item : cart) {
            total = total.add(item.getLineTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private long parseSessionUserId(HttpSession session) {
        if (session == null) {
            return -1;
        }
        Object userIdObj = session.getAttribute("loggedInUserId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(userIdObj));
        } catch (Exception e) {
            return -1;
        }
    }

    private BigDecimal parsePercentage(String value, String field) {
        String v = trim(value);
        if (v.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal pct = new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);
            if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException(field + " must be between 0 and 100.");
            }
            return pct;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid number.");
        }
    }

    private long parseLongRequired(String value, String errorMessage) {
        try {
            long parsed = Long.parseLong(trim(value));
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int parseIntRequired(String value, String errorMessage) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
