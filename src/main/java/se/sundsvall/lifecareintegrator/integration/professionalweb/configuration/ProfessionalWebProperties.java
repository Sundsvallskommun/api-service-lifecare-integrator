package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

/**
 * Configuration for Lifecare ProfessionalWeb. Nothing is required: without a {@code url} the integration is off and
 * every call answers 502, so environments that never talk to ProfessionalWeb start without any of it.
 */
@ConfigurationProperties("integration.lifecare-professionalweb")
public record ProfessionalWebProperties(
	String url,
	String domain,
	@DefaultValue("Actor_Professional") String actor,
	@DefaultValue("saml") String idpMethod,
	String federationProfile,
	String username,
	String password,
	@DefaultValue("20m") Duration sessionTtl,
	@DefaultValue("10") int connectTimeout,
	@DefaultValue("30") int readTimeout,
	String sessionCookie,
	@DefaultValue("5m") Duration keepAliveInterval) {

	/**
	 * Whether this environment talks to ProfessionalWeb at all.
	 *
	 * @return true when a host is configured
	 */
	public boolean isConfigured() {
		return StringUtils.hasText(url);
	}

	/**
	 * Whether a signed-in session is handed in rather than established by signing in.
	 *
	 * @return true when a session cookie is configured
	 */
	public boolean hasSeededSession() {
		return StringUtils.hasText(sessionCookie);
	}

	/**
	 * Whether an integration account is configured to sign in with.
	 *
	 * @return true when both username and password are set
	 */
	public boolean hasAccount() {
		return StringUtils.hasText(username) && StringUtils.hasText(password);
	}

	/**
	 * The host without a trailing slash, so paths can be appended to it.
	 *
	 * @return the base url, or an empty string when unconfigured
	 */
	public String baseUrl() {
		if (!isConfigured()) {
			return "";
		}
		var trimmed = url;
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}
}
