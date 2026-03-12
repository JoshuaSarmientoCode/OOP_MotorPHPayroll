package model;


import service.ValidationService;

public interface Validatable {

    // ========== CORE VALIDATION ==========

    ValidationService.ValidationResult validate();

    boolean isValid();
}