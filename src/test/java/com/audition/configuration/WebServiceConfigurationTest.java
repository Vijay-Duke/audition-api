package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@DisplayName("WebServiceConfiguration Tests")
class WebServiceConfigurationTest {

    private WebServiceConfiguration configuration;

    @BeforeEach
    void setUp() {
        final AuditionApiProperties apiProperties = new AuditionApiProperties();
        apiProperties.setBaseUrl("https://api.example.com");
        apiProperties.setPostsPath("/posts");
        apiProperties.setCommentsPath("/comments");
        apiProperties.setConnectTimeoutMs(5000);
        apiProperties.setReadTimeoutMs(10_000);
        configuration = new WebServiceConfiguration(apiProperties);
    }

    @Nested
    @DisplayName("objectMapper() Tests")
    class ObjectMapperTests {

        @Test
        @DisplayName("Should create ObjectMapper bean")
        void shouldCreateObjectMapperBean() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper).isNotNull();
        }

        @Test
        @DisplayName("Should configure ObjectMapper to not fail on unknown properties")
        void shouldConfigureNotFailOnUnknownProperties() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper.getDeserializationConfig()
                .isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        }

        @Test
        @DisplayName("Should configure ObjectMapper with lower camel case naming strategy")
        void shouldConfigureLowerCamelCaseNaming() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper.getPropertyNamingStrategy())
                .isEqualTo(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        }

        @Test
        @DisplayName("Should configure ObjectMapper to exclude null values")
        void shouldExcludeNullValues() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper.getSerializationConfig()
                .getDefaultPropertyInclusion().getValueInclusion())
                .isEqualTo(JsonInclude.Include.NON_EMPTY);
        }

        @Test
        @DisplayName("Should configure ObjectMapper to not write dates as timestamps")
        void shouldNotWriteDatesAsTimestamps() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper.getSerializationConfig()
                .isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        }

        @Test
        @DisplayName("Should register JavaTimeModule for Java 8 date/time support")
        void shouldRegisterJavaTimeModule() {
            // Act
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Assert
            assertThat(objectMapper.getRegisteredModuleIds())
                .anyMatch(id -> id.toString().contains("jackson-datatype-jsr310"));
        }
    }

    @Nested
    @DisplayName("restTemplate() Tests")
    class RestTemplateTests {

        @Test
        @DisplayName("Should create RestTemplate bean")
        void shouldCreateRestTemplateBean() {
            // Arrange
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Act
            final RestTemplate restTemplate = configuration.restTemplate(objectMapper);

            // Assert
            assertThat(restTemplate).isNotNull();
        }

        @Test
        @DisplayName("Should configure RestTemplate with Jackson message converter")
        void shouldConfigureJacksonMessageConverter() {
            // Arrange
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Act
            final RestTemplate restTemplate = configuration.restTemplate(objectMapper);

            // Assert - RestTemplate preserves default converters, just replaces Jackson one
            assertThat(restTemplate.getMessageConverters())
                .isNotEmpty()
                .anyMatch(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        }

        @Test
        @DisplayName("Should configure RestTemplate with SimpleClientHttpRequestFactory")
        void shouldConfigureSimpleRequestFactory() {
            // Arrange
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Act
            final RestTemplate restTemplate = configuration.restTemplate(objectMapper);

            // Assert
            assertThat(restTemplate.getRequestFactory()).isNotNull();
        }

        @Test
        @DisplayName("RestTemplate message converter should use provided ObjectMapper")
        void restTemplateMessageConverterShouldUseProvidedObjectMapper() {
            // Arrange
            final ObjectMapper objectMapper = configuration.objectMapper();

            // Act
            final RestTemplate restTemplate = configuration.restTemplate(objectMapper);

            // Assert - find the Jackson converter and verify it uses our ObjectMapper
            final MappingJackson2HttpMessageConverter jacksonConverter = restTemplate.getMessageConverters()
                .stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .findFirst()
                .orElseThrow();
            assertThat(jacksonConverter.getObjectMapper()).isEqualTo(objectMapper);
        }
    }
}
