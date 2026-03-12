package model;

import java.time.LocalDate;


public interface Approvable {

    // ========== ACTIONS ==========

    void approve(String approvedBy);

    void reject(String rejectedBy, String reason);

    // ========== STATUS CHECKS ==========

    boolean isPending();

    boolean isApproved();

    boolean isRejected();

    String getStatusDisplay();

    // ========== METADATA ==========

    String getRequestId();

    String getEmployeeId();

    LocalDate getRequestDate();
}