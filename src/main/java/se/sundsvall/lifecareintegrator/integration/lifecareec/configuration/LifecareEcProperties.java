package se.sundsvall.lifecareintegrator.integration.lifecareec.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the Lifecare Welfare API Services / Elderly care (EC) integration. {@code url} is the EC base path
 * (host + {@code /WE.EC.Integration.Host}); {@code domain} and {@code key} are the tenant id and API key applied as
 * query parameters / {@code X-API-Key} header by {@link LifecareEcConfiguration}. The key is sensitive — keep it in a
 * secret, never in committed config, and out of request logging.
 */
@Validated
@ConfigurationProperties(prefix = "integration.lifecare-ec")
public record LifecareEcProperties(

	@NotBlank String url,

	@NotBlank String domain,

	@NotBlank String key,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("30") int readTimeout) {
}
