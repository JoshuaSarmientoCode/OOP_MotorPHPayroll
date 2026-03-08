package model;

public class GovernmentIds {
    private String sssNumber;
    private String tinNumber;
    private String philHealthNumber;
    private String pagIbigNumber;

    // ========== GETTERS ==========

    public String getSssNumber() { return sssNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPhilHealthNumber() { return philHealthNumber; }
    public String getPagIbigNumber() { return pagIbigNumber; }

    // ========== SETTERS WITH FLEXIBLE VALIDATION ==========

    public void setSssNumber(String sssNumber) {
        // Accept with or without dashes
        this.sssNumber = sssNumber;
    }

    public void setTinNumber(String tinNumber) {
        // Accept with or without dashes
        this.tinNumber = tinNumber;
    }

    public void setPhilHealthNumber(String philHealthNumber) {
        // Accept with or without dashes
        this.philHealthNumber = philHealthNumber;
    }

    public void setPagIbigNumber(String pagIbigNumber) {
        // Accept with or without dashes
        this.pagIbigNumber = pagIbigNumber;
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Check if all IDs are present
     */
    public boolean isComplete() {
        return sssNumber != null && !sssNumber.isEmpty() &&
                tinNumber != null && !tinNumber.isEmpty() &&
                philHealthNumber != null && !philHealthNumber.isEmpty() &&
                pagIbigNumber != null && !pagIbigNumber.isEmpty();
    }

    /**
     * Get count of missing IDs
     */
    public int getMissingCount() {
        int count = 0;
        if (sssNumber == null || sssNumber.isEmpty()) count++;
        if (tinNumber == null || tinNumber.isEmpty()) count++;
        if (philHealthNumber == null || philHealthNumber.isEmpty()) count++;
        if (pagIbigNumber == null || pagIbigNumber.isEmpty()) count++;
        return count;
    }

    /**
     * Get cleaned number (remove dashes)
     */
    public String getCleanSss() {
        if (sssNumber == null) return "";
        return sssNumber.replace("-", "");
    }

    public String getCleanTin() {
        if (tinNumber == null) return "";
        return tinNumber.replace("-", "");
    }

    public String getCleanPhilHealth() {
        if (philHealthNumber == null) return "";
        return philHealthNumber.replace("-", "");
    }

    public String getCleanPagIbig() {
        if (pagIbigNumber == null) return "";
        return pagIbigNumber.replace("-", "");
    }

    // ========== FORMATTING METHODS FOR DISPLAY ==========

    public String getFormattedSss() {
        if (sssNumber == null || sssNumber.isEmpty()) return "";
        String clean = sssNumber.replace("-", "");
        if (clean.length() == 10) {
            return clean.substring(0, 2) + "-" +
                    clean.substring(2, 9) + "-" +
                    clean.substring(9);
        }
        return sssNumber; // Return as-is if not 10 digits
    }

    public String getFormattedTin() {
        if (tinNumber == null || tinNumber.isEmpty()) return "";
        String clean = tinNumber.replace("-", "");
        if (clean.length() == 12) {
            return clean.substring(0, 3) + "-" +
                    clean.substring(3, 6) + "-" +
                    clean.substring(6, 9) + "-" +
                    clean.substring(9);
        }
        return tinNumber;
    }

    public String getFormattedPhilHealth() {
        if (philHealthNumber == null || philHealthNumber.isEmpty()) return "";
        String clean = philHealthNumber.replace("-", "");
        if (clean.length() == 12) {
            return clean.substring(0, 2) + "-" +
                    clean.substring(2, 11) + "-" +
                    clean.substring(11);
        }
        return philHealthNumber;
    }

    public String getFormattedPagIbig() {
        if (pagIbigNumber == null || pagIbigNumber.isEmpty()) return "";
        String clean = pagIbigNumber.replace("-", "");
        if (clean.length() == 12) {
            return clean.substring(0, 4) + "-" +
                    clean.substring(4, 8) + "-" +
                    clean.substring(8);
        }
        return pagIbigNumber;
    }

    /**
     * Get formatted string with mask (show only last 4 characters)
     */
    public String getMaskedSss() {
        String formatted = getFormattedSss();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }

    public String getMaskedTin() {
        String formatted = getFormattedTin();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }

    public String getMaskedPhilHealth() {
        String formatted = getFormattedPhilHealth();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }

    public String getMaskedPagIbig() {
        String formatted = getFormattedPagIbig();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }
}