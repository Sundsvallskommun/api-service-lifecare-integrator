package se.sundsvall.lifecareintegrator.integration.lifecarefc.configuration;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import static java.util.Optional.ofNullable;

/**
 * Configuration for the Tieto/Lifecare FamilyCare (FC) integration. {@code url} is the FC base path (host +
 * {@code /WESE.FC.Api.FC}); {@code domain} and {@code key} are the FC tenant id and API key applied as query
 * parameters by {@link LifecareFcConfiguration}. The keys are secrets — keep them out of committed config and out of
 * request logging.
 *
 * <p>
 * FC licences its APIs per consumer, and the {@code Users/*} directory is a different licence from the person-based
 * case APIs — each key is 401 on the other's endpoints. {@code userKey} carries that second licence key and is
 * optional: leave it unset where one consumer covers both surfaces, and {@code key} is used for every call.
 */
@Validated
@ConfigurationProperties(prefix = "integration.lifecare-fc")
public record LifecareFcProperties(

	@NotBlank String url,

	@NotBlank String domain,

	@NotBlank String key,

	String userKey,

	@DefaultValue("5") int connectTimeout,

	@DefaultValue("30") int readTimeout,

	@DefaultValue("NONE") String logLevel) {

	/**
	 * The key to authenticate the {@code Users/*} endpoints with: the separate user-directory licence key when one is
	 * configured, otherwise the main {@link #key()}.
	 *
	 * @return the user-directory key, never blank
	 */
	public String userKeyOrDefault() {
		return ofNullable(userKey).filter(StringUtils::hasText).orElse(key);
	}
}
