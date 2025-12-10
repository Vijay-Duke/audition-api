package com.audition;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.service.AuditionService;
import com.audition.web.AuditionController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@DisplayName("AuditionApplication Integration Tests")
class AuditionApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("AuditionController bean should be present")
    void auditionControllerBeanShouldBePresent() {
        assertThat(applicationContext.getBean(AuditionController.class)).isNotNull();
    }

    @Test
    @DisplayName("AuditionService bean should be present")
    void auditionServiceBeanShouldBePresent() {
        assertThat(applicationContext.getBean(AuditionService.class)).isNotNull();
    }

    @Test
    @DisplayName("AuditionIntegrationClient bean should be present")
    void auditionIntegrationClientBeanShouldBePresent() {
        assertThat(applicationContext.getBean(AuditionIntegrationClient.class)).isNotNull();
    }
}
