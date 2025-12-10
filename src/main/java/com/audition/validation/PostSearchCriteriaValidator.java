package com.audition.validation;

import com.audition.model.PostSearchCriteria;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostSearchCriteriaValidator implements ConstraintValidator<ValidPostSearchCriteria, PostSearchCriteria> {

    @Override
    public boolean isValid(final PostSearchCriteria criteria, final ConstraintValidatorContext context) {
        if (criteria == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (!validatePageAndSizeTogether(criteria, context)) {
            isValid = false;
        }

        if (!validateOrderRequiresSort(criteria, context)) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validatePageAndSizeTogether(final PostSearchCriteria criteria,
                                                 final ConstraintValidatorContext context) {
        final boolean pageProvided = criteria.getPage() != null;
        final boolean sizeProvided = criteria.getSize() != null;

        if (pageProvided != sizeProvided) {
            context.buildConstraintViolationWithTemplate("Both page and size must be provided together")
                .addPropertyNode(pageProvided ? "size" : "page")
                .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validateOrderRequiresSort(final PostSearchCriteria criteria,
                                               final ConstraintValidatorContext context) {
        if (criteria.getOrder() != null && criteria.getSort() == null) {
            context.buildConstraintViolationWithTemplate("Sort field is required when order is specified")
                .addPropertyNode("sort")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
