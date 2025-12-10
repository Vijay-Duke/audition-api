package com.audition.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PostSearchCriteria Tests")
@SuppressWarnings("PMD.TooManyMethods") // Test class with comprehensive coverage
class PostSearchCriteriaTest {

    private static final String SORT_TITLE = "title";
    private static final String ORDER_ASC = "asc";
    private static final String ORDER_DESC = "desc";
    private static final String TITLE_CONTAINS_TEST = "test";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Nested
    @DisplayName("userId validation")
    class UserIdValidationTests {

        @Test
        @DisplayName("Should pass when userId is null")
        void shouldPassWhenUserIdIsNull() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass when userId is positive")
        void shouldPassWhenUserIdIsPositive() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest(name = "userId = {0} should be invalid")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should fail when userId is zero or negative")
        void shouldFailWhenUserIdIsZeroOrNegative(final int userId) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(userId)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("User id must be a positive integer");
        }
    }

    @Nested
    @DisplayName("pagination validation")
    class PaginationValidationTests {

        @Test
        @DisplayName("Should pass when both page and size are null")
        void shouldPassWhenBothPageAndSizeAreNull() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass when both page and size are provided and valid")
        void shouldPassWhenBothPageAndSizeAreValid() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(10)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when only page is provided")
        void shouldFailWhenOnlyPageIsProvided() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Both page and size must be provided together");
        }

        @Test
        @DisplayName("Should fail when only size is provided")
        void shouldFailWhenOnlySizeIsProvided() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .size(10)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Both page and size must be provided together");
        }

        @ParameterizedTest(name = "page = {0} should be invalid")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should fail when page is less than 1")
        void shouldFailWhenPageIsLessThanOne(final int page) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(page)
                .size(10)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Page number must be at least 1"))).isTrue();
        }

        @ParameterizedTest(name = "size = {0} should be invalid")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Should fail when size is zero or negative")
        void shouldFailWhenSizeIsZeroOrNegative(final int size) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(size)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Page size must be a positive integer"))).isTrue();
        }

        @ParameterizedTest(name = "size = {0} should exceed maximum")
        @ValueSource(ints = {101, 500, 1_000_000})
        @DisplayName("Should fail when size exceeds maximum of 100")
        void shouldFailWhenSizeExceedsMaximum(final int size) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(size)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isNotEmpty();
            assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Page size cannot exceed 100"))).isTrue();
        }

        @Test
        @DisplayName("Should accept page 1 with valid size")
        void shouldAcceptPageOne() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .page(1)
                .size(10)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("sort validation")
    class SortValidationTests {

        @Test
        @DisplayName("Should pass when sort is null")
        void shouldPassWhenSortIsNull() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest(name = "sort = {0} should be valid")
        @ValueSource(strings = {"id", "userId", "title"})
        @DisplayName("Should pass when sort is a valid field")
        void shouldPassWhenSortIsValidField(final String sort) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort(sort)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest(name = "sort = {0} should be invalid")
        @ValueSource(strings = {"invalid", "body", "email", "name"})
        @DisplayName("Should fail when sort field is invalid")
        void shouldFailWhenSortFieldIsInvalid(final String sort) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort(sort)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Sort field must be one of:");
        }

        @Test
        @DisplayName("Should pass when sort and order are both provided")
        void shouldPassWhenSortAndOrderAreBothProvided() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .order(ORDER_DESC)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass when only sort is provided")
        void shouldPassWhenOnlySortIsProvided() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when only order is provided")
        void shouldFailWhenOnlyOrderIsProvided() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .order(ORDER_DESC)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Sort field is required when order is specified");
        }

        @ParameterizedTest(name = "order = {0} should be valid")
        @ValueSource(strings = {"asc", "desc", "ASC", "DESC", "Asc", "Desc"})
        @DisplayName("Should accept valid order values case-insensitively")
        void shouldAcceptValidOrderValuesCaseInsensitively(final String order) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .order(order)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest(name = "order = {0} should be invalid")
        @ValueSource(strings = {"ascending", "descending", "up", "down", "invalid"})
        @DisplayName("Should fail when order is invalid")
        void shouldFailWhenOrderIsInvalid(final String order) {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .sort("id")
                .order(order)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Order must be 'asc' or 'desc'");
        }
    }

    @Nested
    @DisplayName("combined validation")
    class CombinedValidationTests {

        @Test
        @DisplayName("Should pass with all valid parameters")
        void shouldPassWithAllValidParameters() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .page(1)
                .size(10)
                .sort("id")
                .order(ORDER_DESC)
                .build();

            // Act
            final Set<ConstraintViolation<PostSearchCriteria>> violations = validator.validate(criteria);

            // Assert
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("builder and immutability")
    class BuilderTests {

        @Test
        @DisplayName("Should create criteria with builder")
        void shouldCreateCriteriaWithBuilder() {
            // Act
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .page(2)
                .size(20)
                .sort(SORT_TITLE)
                .order(ORDER_ASC)
                .build();

            // Assert
            assertThat(criteria.getUserId()).isEqualTo(1);
            assertThat(criteria.getTitleContains()).isEqualTo(TITLE_CONTAINS_TEST);
            assertThat(criteria.getPage()).isEqualTo(2);
            assertThat(criteria.getSize()).isEqualTo(20);
            assertThat(criteria.getSort()).isEqualTo(SORT_TITLE);
            assertThat(criteria.getOrder()).isEqualTo(ORDER_ASC);
        }

        @Test
        @DisplayName("Should create empty criteria with defaults")
        void shouldCreateEmptyCriteriaWithDefaults() {
            // Act
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();

            // Assert
            assertThat(criteria.getUserId()).isNull();
            assertThat(criteria.getTitleContains()).isNull();
            assertThat(criteria.getPage()).isNull();
            assertThat(criteria.getSize()).isNull();
            assertThat(criteria.getSort()).isNull();
            assertThat(criteria.getOrder()).isNull();
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenAllFieldsMatch() {
            // Arrange
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .page(1)
                .size(10)
                .sort("id")
                .order(ORDER_ASC)
                .build();

            final PostSearchCriteria criteria2 = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .page(1)
                .size(10)
                .sort("id")
                .order(ORDER_ASC)
                .build();

            // Assert
            assertThat(criteria1).isEqualTo(criteria2);
            assertThat(criteria1.hashCode()).isEqualTo(criteria2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when userId differs")
        void shouldNotBeEqualWhenUserIdDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().userId(1).build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().userId(2).build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal when titleContains differs")
        void shouldNotBeEqualWhenTitleContainsDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().titleContains("a").build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().titleContains("b").build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal when page differs")
        void shouldNotBeEqualWhenPageDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().page(1).size(10).build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().page(2).size(10).build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal when size differs")
        void shouldNotBeEqualWhenSizeDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().page(1).size(10).build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().page(1).size(20).build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal when sort differs")
        void shouldNotBeEqualWhenSortDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().sort("id").build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().sort(SORT_TITLE).build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal when order differs")
        void shouldNotBeEqualWhenOrderDiffers() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().sort("id").order(ORDER_ASC).build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().sort("id").order(ORDER_DESC).build();

            assertThat(criteria1).isNotEqualTo(criteria2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();
            assertThat(criteria).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();
            assertThat(criteria).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            final PostSearchCriteria criteria = PostSearchCriteria.builder().userId(1).build();
            assertThat(criteria).isEqualTo(criteria);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .build();

            final int hashCode1 = criteria.hashCode();
            final int hashCode2 = criteria.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Empty criteria should be equal")
        void emptyCriteriaShouldBeEqual() {
            final PostSearchCriteria criteria1 = PostSearchCriteria.builder().build();
            final PostSearchCriteria criteria2 = PostSearchCriteria.builder().build();

            assertThat(criteria1).isEqualTo(criteria2);
            assertThat(criteria1.hashCode()).isEqualTo(criteria2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString with all fields")
        void shouldGenerateMeaningfulToStringWithAllFields() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(1)
                .titleContains(TITLE_CONTAINS_TEST)
                .page(2)
                .size(20)
                .sort(SORT_TITLE)
                .order(ORDER_ASC)
                .build();

            // Act
            final String toString = criteria.toString();

            // Assert
            assertThat(toString).contains("userId=1");
            assertThat(toString).contains("titleContains=test");
            assertThat(toString).contains("page=2");
            assertThat(toString).contains("size=20");
            assertThat(toString).contains("sort=title");
            assertThat(toString).contains("order=asc");
        }

        @Test
        @DisplayName("Should generate toString with null fields")
        void shouldGenerateToStringWithNullFields() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder().build();

            // Act
            final String toString = criteria.toString();

            // Assert
            assertThat(toString).contains("PostSearchCriteria");
            assertThat(toString).contains("userId=null");
        }

        @Test
        @DisplayName("Should generate toString with partial fields")
        void shouldGenerateToStringWithPartialFields() {
            // Arrange
            final PostSearchCriteria criteria = PostSearchCriteria.builder()
                .userId(5)
                .sort("id")
                .build();

            // Act
            final String toString = criteria.toString();

            // Assert
            assertThat(toString).contains("userId=5");
            assertThat(toString).contains("sort=id");
            assertThat(toString).contains("page=null");
        }
    }
}
