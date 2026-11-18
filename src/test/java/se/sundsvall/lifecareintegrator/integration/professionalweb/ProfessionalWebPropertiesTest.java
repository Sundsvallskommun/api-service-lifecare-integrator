package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers what {@link ProfessionalWebProperties#baseUrl()} accepts and rejects, since S8786 replaced the trailing-slash
 * regex with a plain loop: a host with no trailing slash, one trailing slash, several trailing slashes, and a slash
 * that is not trailing should all behave the same as before.
 */
class ProfessionalWebPropertiesTest {

	@Test
	void baseUrlKeepsAHostWithNoTrailingSlash() {
		final var properties = properties("https://lifecare.sundsvall.se");

		assertThat(properties.baseUrl()).isEqualTo("https://lifecare.sundsvall.se");
	}

	@Test
	void baseUrlStripsOneTrailingSlash() {
		final var properties = properties("https://lifecare.sundsvall.se/");

		assertThat(properties.baseUrl()).isEqualTo("https://lifecare.sundsvall.se");
	}

	@Test
	void baseUrlStripsSeveralTrailingSlashes() {
		final var properties = properties("https://lifecare.sundsvall.se///");

		assertThat(properties.baseUrl()).isEqualTo("https://lifecare.sundsvall.se");
	}

	@Test
	void baseUrlKeepsASlashThatIsNotTrailing() {
		final var properties = properties("https://lifecare.sundsvall.se/WE.Flow.Html");

		assertThat(properties.baseUrl()).isEqualTo("https://lifecare.sundsvall.se/WE.Flow.Html");
	}

	@Test
	void baseUrlIsEmptyWhenUnconfigured() {
		final var properties = properties(null);

		assertThat(properties.baseUrl()).isEmpty();
	}

	private static ProfessionalWebProperties properties(final String url) {
		return new ProfessionalWebProperties(url, null, "Actor_Professional", "saml", null, null, null, Duration.ofMinutes(20), 10, 30, null, Duration.ofMinutes(5));
	}
}
