package se.sundsvall.lifecareintegrator.integration.elderlycare.configuration;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ElderlyCarePropertiesTest {

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void accessors() {
		final var properties = new ElderlyCareProperties("http://ec", "the-domain", "the-key", 5, 30, "FULL");

		assertThat(properties.url()).isEqualTo("http://ec");
		assertThat(properties.domain()).isEqualTo("the-domain");
		assertThat(properties.key()).isEqualTo("the-key");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
		assertThat(properties.logLevel()).isEqualTo("FULL");
	}

	@Test
	void aPercentEncodedKeyIsAccepted() {
		final var properties = new ElderlyCareProperties("http://ec", "the-domain", "ab%2Bcd%2Fef%3D", 5, 30, "NONE");

		assertThat(VALIDATOR.validate(properties)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"ab+cd", "ab/cd", "abcd="
	})
	void aDecodedKeyIsRejected(final String key) {
		final var properties = new ElderlyCareProperties("http://ec", "the-domain", key, 5, 30, "NONE");

		assertThat(VALIDATOR.validate(properties))
			.extracting(violation -> violation.getPropertyPath().toString())
			.containsExactly("key");
	}
}
