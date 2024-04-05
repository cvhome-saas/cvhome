package com.asrevo.cvhome.store.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private BeanUtils beanUtils;

    public void initialize(final FieldMatch constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
        this.beanUtils = BeanUtils.newInstance();
    }

    @SuppressWarnings("nls")
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        try {
            final Object firstObj = this.beanUtils.getPropertyValue(value, this.firstFieldName);
            final Object secondObj = this.beanUtils.getPropertyValue(value, this.secondFieldName);
            return firstObj == null && secondObj == null || firstObj != null && firstObj.equals(secondObj);
        } catch (final Exception ex) {
            log.info("Error while getting values from object", ex);
            return false;

        }

    }
}
