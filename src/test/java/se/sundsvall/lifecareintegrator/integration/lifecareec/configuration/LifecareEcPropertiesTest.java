package se.sundsvall.lifecareintegrator.integration.lifecareec.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LifecareEcPropertiesTest {

	@Test
	void accessors() {
		final var properties = new LifecareEcProperties("http://ec", "the-domain", "the-key", 5, 30);

		assertThat(properties.url()).isEqualTo("http://ec");
		assertThat(properties.domain()).isEqualTo("the-domain");
		assertThat(properties.key()).isEqualTo("the-key");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
	}
}
