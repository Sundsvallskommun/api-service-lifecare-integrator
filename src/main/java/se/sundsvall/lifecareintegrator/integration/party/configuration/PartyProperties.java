package se.sundsvall.lifecareintegrator.integration.party.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("integration.party")
public record PartyProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("30") int readTimeout,
	@DefaultValue("1000") int maxPartyIdsPerCall,
	@DefaultValue("1000") int maxLegalIdsPerCall,
	@DefaultValue("PT15M") Duration cacheTimeToLive,
	@DefaultValue("10000") long cacheMaximumSize) {
}
