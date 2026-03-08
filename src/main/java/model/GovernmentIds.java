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
    
    // ========== SETTERS ==========
    
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public void setPhilHealthNumber(String philHealthNumber) { this.philHealthNumber = philHealthNumber; }
    public void setPagIbigNumber(String pagIbigNumber) { this.pagIbigNumber = pagIbigNumber; }
    
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
    
    // ========== FORMATTING METHODS FOR DISPLAY ==========
    
    public String getFormattedSss() {
        if (sssNumber == null || sssNumber.isEmpty()) return "";
        // Format as XX-XXXXXXX-X if it's a raw number
        if (sssNumber.matches("\\d{10}")) {
            return sssNumber.substring(0, 2) + "-" + 
                   sssNumber.substring(2, 9) + "-" + 
                   sssNumber.substring(9);
        }
        return sssNumber;
    }
    
    public String getFormattedTin() {
        if (tinNumber == null || tinNumber.isEmpty()) return "";
        // Format as XXX-XXX-XXX-XXX if it's a raw number
        if (tinNumber.matches("\\d{12}")) {
            return tinNumber.substring(0, 3) + "-" + 
                   tinNumber.substring(3, 6) + "-" + 
                   tinNumber.substring(6, 9) + "-" + 
                   tinNumber.substring(9);
        }
        return tinNumber;
    }
    
    public String getFormattedPhilHealth() {
        if (philHealthNumber == null || philHealthNumber.isEmpty()) return "";
        // Format as XX-XXXXXXXXX-X if it's a raw number
        if (philHealthNumber.matches("\\d{12}")) {
            return philHealthNumber.substring(0, 2) + "-" + 
                   philHealthNumber.substring(2, 11) + "-" + 
                   philHealthNumber.substring(11);
        }
        return philHealthNumber;
    }
    
    public String getFormattedPagIbig() {
        if (pagIbigNumber == null || pagIbigNumber.isEmpty()) return "";
        // Format as XXXX-XXXX-XXXX if it's a raw number
        if (pagIbigNumber.matches("\\d{12}")) {
            return pagIbigNumber.substring(0, 4) + "-" + 
                   pagIbigNumber.substring(4, 8) + "-" + 
                   pagIbigNumber.substring(8);
        }
        return pagIbigNumber;
    }
    
    /**
     * Get formatted string with mask (show only last 4 characters)
     */
    public String getMaskedSss() {
        if (sssNumber == null || sssNumber.isEmpty()) return "";
        String formatted = getFormattedSss();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }
    
    public String getMaskedTin() {
        if (tinNumber == null || tinNumber.isEmpty()) return "";
        String formatted = getFormattedTin();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }
    
    public String getMaskedPhilHealth() {
        if (philHealthNumber == null || philHealthNumber.isEmpty()) return "";
        String formatted = getFormattedPhilHealth();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }
    
    public String getMaskedPagIbig() {
        if (pagIbigNumber == null || pagIbigNumber.isEmpty()) return "";
        String formatted = getFormattedPagIbig();
        if (formatted.length() > 4) {
            return "••••••" + formatted.substring(formatted.length() - 4);
        }
        return formatted;
    }
}