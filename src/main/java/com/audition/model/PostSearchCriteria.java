package com.audition.model;

import com.audition.validation.ValidPostSearchCriteria;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@ValidPostSearchCriteria
public class PostSearchCriteria {

    @Positive(message = "User id must be a positive integer")
    Integer userId;

    String titleContains;

    @Min(value = 1, message = "Page number must be at least 1")
    Integer page;

    @Positive(message = "Page size must be a positive integer")
    Integer size;

    @Pattern(regexp = "^(id|userId|title)$", message = "Sort field must be one of: id, userId, title")
    String sort;

    @Pattern(regexp = "^(?i)(asc|desc)$", message = "Order must be 'asc' or 'desc'")
    String order;
}
