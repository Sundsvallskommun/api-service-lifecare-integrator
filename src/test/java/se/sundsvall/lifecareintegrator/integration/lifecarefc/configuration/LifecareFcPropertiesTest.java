package se.sundsvall.lifecareintegrator.integration.lifecarefc.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LifecareFcPropertiesTest {

	@Test
	void accessors() {
		final var properties = new LifecareFcProperties("http://fc", "the-domain", "the-key", "the-user-key", 5, 30, "FULL");

		assertThat(properties.url()).isEqualTo("http://fc");
		assertThat(properties.domain()).isEqualTo("the-domain");
		assertThat(properties.key()).isEqualTo("the-key");
		assertThat(properties.userKey()).isEqualTo("the-user-key");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
		assertThat(properties.logLevel()).isEqualTo("FULL");
	}

	@Test
	void userKeyOrDefaultUsesTheUserKeyWhenConfigured() {
		final var properties = new LifecareFcProperties("http://fc", "the-domain", "the-key", "the-user-key", 5, 30, "NONE");

		assertThat(properties.userKeyOrDefault()).isEqualTo("the-user-key");
	}

	@Test
	void userKeyOrDefaultFallsBackToTheMainKeyWhenUnsetOrBlank() {
		final var unset = new LifecareFcProperties("http://fc", "the-domain", "the-key", null, 5, 30, "NONE");
		final var blank = new LifecareFcProperties("http://fc", "the-domain", "the-key", "   ", 5, 30, "NONE");

		assertThat(unset.userKeyOrDefault()).isEqualTo("the-key");
		assertThat(blank.userKeyOrDefault()).isEqualTo("the-key");
	}
}
