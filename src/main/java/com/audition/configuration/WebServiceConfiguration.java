package com.audition.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AuditionApiProperties.class)
public class WebServiceConfiguration implements WebMvcConfigurer {

    private final AuditionApiProperties apiProperties;

    public WebServiceConfiguration(final AuditionApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate(final ObjectMapper objectMapper) {
        final RestTemplate restTemplate = new RestTemplate(createClientFactory());
        restTemplate.getMessageConverters().replaceAll(converter ->
            converter instanceof MappingJackson2HttpMessageConverter
                ? new MappingJackson2HttpMessageConverter(objectMapper)
                : converter
        );
        restTemplate.getInterceptors().add(new RestTemplateLoggingInterceptor());
        return restTemplate;
    }

    private SimpleClientHttpRequestFactory createClientFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(apiProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(apiProperties.getReadTimeoutMs());
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }
}
