package se.sundsvall.lifecareintegrator.integration.elderlycare.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.elderlycare")
public record ElderlyCareProperties(

	@NotBlank String url,

	@NotBlank String domain,

	@NotBlank @Pattern(regexp = "[^+/=]*", message = "must be percent-encoded") String key,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("30") int readTimeout,

	@DefaultValue("NONE") String logLevel) {
}
