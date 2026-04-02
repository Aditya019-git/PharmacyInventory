package com.pharmacyinventory.controller;

import com.pharmacyinventory.dao.MedicineDao;
import com.pharmacyinventory.model.Medicine;
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

@WebServlet("/medicines")
public class MedicineServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MedicineDao medicineDao = new MedicineDao();

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
                medicineDao.delete(id);
                response.sendRedirect(request.getContextPath() + "/medicines?success="
                        + encode("Medicine deleted successfully."));
                return;
            }

            if ("edit".equalsIgnoreCase(action) && idParam != null) {
                long id = Long.parseLong(idParam);
                Optional<Medicine> medicine = medicineDao.findById(id);
                medicine.ifPresent(value -> request.setAttribute("editMedicine", value));
            }

            List<Medicine> medicines = medicineDao.findAll();
            request.setAttribute("medicines", medicines);
            request.getRequestDispatcher("/views/medicines.jsp").forward(request, response);
        } catch (NumberFormatException | SQLException e) {
            request.setAttribute("error", "Unable to process medicine request.");
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

        String idParam = request.getParameter("id");
        String medicineCode = trim(request.getParameter("medicineCode"));
        String name = trim(request.getParameter("name"));
        String category = trim(request.getParameter("category"));
        String brand = trim(request.getParameter("brand"));
        String unit = trim(request.getParameter("unit"));
        String reorderLevelParam = trim(request.getParameter("reorderLevel"));

        if (isBlank(medicineCode) || isBlank(name) || isBlank(unit) || isBlank(reorderLevelParam)) {
            request.setAttribute("error", "Medicine code, name, unit and reorder level are required.");
            request.setAttribute("editMedicine", buildMedicineFromInput(idParam, medicineCode, name, category, brand, unit, reorderLevelParam));
            forwardWithList(request, response);
            return;
        }

        if (!isBlank(idParam) && parseLongOrDefault(idParam, -1) <= 0) {
            request.setAttribute("error", "Invalid medicine ID.");
            request.setAttribute("editMedicine", buildMedicineFromInput("", medicineCode, name, category, brand, unit, reorderLevelParam));
            forwardWithList(request, response);
            return;
        }

        try {
            int reorderLevel = Integer.parseInt(reorderLevelParam);
            Medicine medicine = buildMedicineFromInput(idParam, medicineCode, name, category, brand, unit, reorderLevelParam);
            medicine.setReorderLevel(reorderLevel);
            boolean saved;

            if (isBlank(idParam)) {
                saved = medicineDao.create(medicine);
            } else {
                saved = medicineDao.update(medicine);
            }

            if (!saved) {
                request.setAttribute("error", "No changes were saved. Please try again.");
                request.setAttribute("editMedicine", medicine);
                forwardWithList(request, response);
                return;
            }

            if (isBlank(idParam)) {
                response.sendRedirect(request.getContextPath() + "/medicines?success="
                        + encode("Medicine added successfully."));
            } else {
                response.sendRedirect(request.getContextPath() + "/medicines?success="
                        + encode("Medicine updated successfully."));
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Reorder level must be a valid number.");
            request.setAttribute("editMedicine", buildMedicineFromInput(idParam, medicineCode, name, category, brand, unit, reorderLevelParam));
            forwardWithList(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Unable to save medicine. Code may already exist.");
            request.setAttribute("editMedicine", buildMedicineFromInput(idParam, medicineCode, name, category, brand, unit, reorderLevelParam));
            forwardWithList(request, response);
        }
    }

    private Medicine buildMedicineFromInput(String idParam, String medicineCode, String name, String category,
                                            String brand, String unit, String reorderLevelParam) {
        Medicine medicine = new Medicine();
        if (!isBlank(idParam)) {
            medicine.setId(parseLongOrDefault(idParam, 0));
        }
        medicine.setMedicineCode(medicineCode);
        medicine.setName(name);
        medicine.setCategory(category);
        medicine.setBrand(brand);
        medicine.setUnit(unit);
        medicine.setReorderLevel(parseIntOrDefault(reorderLevelParam, 0));
        return medicine;
    }

    private void forwardWithList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("medicines", medicineDao.findAll());
        } catch (SQLException e) {
            request.setAttribute("medicines", Collections.emptyList());
        }
        request.getRequestDispatcher("/views/medicines.jsp").forward(request, response);
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

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long parseLongOrDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
