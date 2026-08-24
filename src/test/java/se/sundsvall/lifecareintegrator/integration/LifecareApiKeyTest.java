package se.sundsvall.lifecareintegrator.integration;

import java.net.URLDecoder;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.lifecareintegrator.integration.LifecareApiKey.forQuery;

class LifecareApiKeyTest {

	private static final String DECODED = "B3zXYTywcGodU+eJwc7ZJut8IcVXV/abc=";
	private static final String ENCODED = "B3zXYTywcGodU%2beJwc7ZJut8IcVXV%2fabc%3d";

	@Test
	void bothConfiguredFormsProduceTheSameQueryValue() {
		assertThat(forQuery(DECODED)).isEqualTo(forQuery(ENCODED));
	}

	@Test
	void theQueryValueNeverCarriesARawPlus() {
		// A raw '+' is decoded by the server as a space, and Feign does not encode it for us.
		assertThat(forQuery(DECODED))
			.doesNotContain("+")
			.contains("%2B");
	}

	@Test
	void theEncodedValueRoundTripsToTheRealSecret() {
		assertThat(URLDecoder.decode(forQuery(ENCODED), UTF_8))
			.isEqualTo(DECODED);
	}

	@Test
	void aKeyWithoutSpecialCharactersIsUntouched() {
		assertThat(forQuery("plainkey123")).isEqualTo("plainkey123");
	}

	@Test
	void nullIsTolerated() {
		assertThat(forQuery(null)).isNull();
	}
}
