package se.sundsvall.lifecareintegrator.integration.familycare.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import static java.util.Optional.ofNullable;

@Validated
@ConfigurationProperties(prefix = "integration.familycare")
public record FamilyCareProperties(

	@NotBlank String url,

	@NotBlank String domain,

	@NotBlank String key,

	String userKey,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("30") int readTimeout,

	@DefaultValue("NONE") String logLevel) {

	public String userKeyOrDefault() {
		return ofNullable(userKey).filter(StringUtils::hasText).orElse(key);
	}
}
