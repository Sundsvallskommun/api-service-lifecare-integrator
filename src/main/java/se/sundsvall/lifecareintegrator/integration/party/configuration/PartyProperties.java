package se.sundsvall.lifecareintegrator.integration.party.configuration;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("integration.party")
public record PartyProperties(
	@NotBlank String url,
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("30") int readTimeout,
	@DefaultValue("PT15M") Duration cacheTimeToLive,
	@DefaultValue("10000") long cacheMaximumSize) {
}
