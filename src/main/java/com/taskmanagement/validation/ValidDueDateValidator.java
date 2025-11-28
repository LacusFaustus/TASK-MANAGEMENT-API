package com.taskmanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class ValidDueDateValidator implements ConstraintValidator<ValidDueDate, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime dueDate, ConstraintValidatorContext context) {
        if (dueDate == null) {
            return true;
        }
        return dueDate.isAfter(LocalDateTime.now());
    }
}
