package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.StringUtils;

/**
 * Configuration for the Lifecare ProfessionalWeb integration: the internal api2 surface Lifecare's own web client
 * talks to, reached with a signed-in session of an integration account rather than an API key.
 *
 * <p>
 * {@code url} is the Lifecare host (for example {@code https://lifecare.sundsvall.se}), without a module path. Leaving
 * it unset turns the integration off: every call then answers 502 instead of the application failing to start, so
 * environments that never talk to ProfessionalWeb need none of this.
 * </p>
 *
 * <p>
 * {@code domain}, {@code actor}, {@code idpMethod} and {@code federationProfile} are what Lifecare's identity portal
 * normally reads off the entry URL on a browser's first visit. {@code username} and {@code password} are the
 * integration account; both are secrets and belong in the environment, never in committed config or a log line.
 * </p>
 *
 * @param url               the Lifecare host
 * @param domain            the Lifecare domain, e.g. SundsvallVoO_PLUS
 * @param actor             the Lifecare actor
 * @param idpMethod         the identity provider method
 * @param federationProfile the federation profile, e.g. Sundsvall_Intra
 * @param username          the integration account
 * @param password          the integration account's password
 * @param sessionTtl        how long a session is used before signing in afresh
 * @param connectTimeout    connect timeout in seconds
 * @param readTimeout       read timeout in seconds
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
	@DefaultValue("30") int readTimeout) {

	/**
	 * Whether this environment talks to ProfessionalWeb at all.
	 *
	 * @return true when a host is configured
	 */
	public boolean isConfigured() {
		return StringUtils.hasText(url);
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
		return url.replaceAll("/+$", "");
	}
}
