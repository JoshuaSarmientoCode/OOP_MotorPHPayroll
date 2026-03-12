package service;


public interface Validatable {

    // ========== CORE VALIDATION ==========

    ValidationService.ValidationResult validate();

    boolean isValid();
}