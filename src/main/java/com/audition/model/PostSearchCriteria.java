package com.audition.model;

import com.audition.validation.ValidPostSearchCriteria;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Search criteria for filtering, paginating, and sorting posts")
public class PostSearchCriteria {

    @Schema(description = "Filter by user ID", example = "1", minimum = "1")
    @Positive(message = "User id must be a positive integer")
    Integer userId;

    @Schema(description = "Filter posts containing this text in title", example = "sunt")
    String titleContains;

    @Schema(description = "Page number (1-based). Must be used together with 'size'", example = "1", minimum = "1")
    @Min(value = 1, message = "Page number must be at least 1")
    Integer page;

    @Schema(description = "Number of items per page. Must be used together with 'page'", example = "10", minimum = "1")
    @Positive(message = "Page size must be a positive integer")
    Integer size;

    @Schema(description = "Field to sort by", example = "id", allowableValues = {"id", "userId", "title"})
    @Pattern(regexp = "^(id|userId|title)$", message = "Sort field must be one of: id, userId, title")
    String sort;

    @Schema(description = "Sort order", example = "asc", allowableValues = {"asc", "desc"})
    @Pattern(regexp = "^(?i)(asc|desc)$", message = "Order must be 'asc' or 'desc'")
    String order;
}
