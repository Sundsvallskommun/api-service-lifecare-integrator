package se.sundsvall.lifecareintegrator.integration.employee.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the api-service-employee integration, used to put a name to a Lifecare caseworker id.
 * {@code domain} is the employee directory the login name belongs to — {@code personal} for municipal staff.
 */
@Validated
@ConfigurationProperties("integration.employee")
public record EmployeeProperties(

	@NotBlank String url,

	@DefaultValue("personal") String domain,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("10") int readTimeout) {
}
