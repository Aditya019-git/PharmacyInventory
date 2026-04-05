package com.pharmacyinventory.model;

public class LowStockReportRow {
    private String medicineCode;
    private String medicineName;
    private int reorderLevel;
    private int availableQty;
    private int shortageQty;

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public int getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(int availableQty) {
        this.availableQty = availableQty;
    }

    public int getShortageQty() {
        return shortageQty;
    }

    public void setShortageQty(int shortageQty) {
        this.shortageQty = shortageQty;
    }
}
