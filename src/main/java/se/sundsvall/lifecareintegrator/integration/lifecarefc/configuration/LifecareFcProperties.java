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
 * case APIs. {@code userKey} carries that second licence key and is optional: leave it unset where one consumer covers
 * both surfaces, and {@code key} is used for every call.
 *
 * <p>
 * <strong>A key licensed for the wrong surface does not fail — it answers {@code 200} with an empty list.</strong>
 * This text claimed a {@code 401} until 2026-09-22, and that claim cost hours: every person-based read came back empty
 * while every source reported OK, and the key was ruled out early precisely because nothing had returned 401. There is
 * no signal to detect this from — not a status, not a log line — so when a person that FamilyCare demonstrably has
 * data for comes back empty through this service, suspect {@code key} before anything else.
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
