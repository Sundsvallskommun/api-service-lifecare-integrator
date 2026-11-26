package se.sundsvall.lifecareintegrator.integration.familycare.configuration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

	@NotBlank @Pattern(regexp = "[^+/=]*", message = "must be percent-encoded") String key,

	@Pattern(regexp = "[^+/=]*", message = "must be percent-encoded") String userKey,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("30") int readTimeout,

	@DefaultValue("NONE") String logLevel) {

	public String getUserKeyOrDefault() {
		return ofNullable(userKey).filter(StringUtils::hasText).orElse(key);
	}
}
