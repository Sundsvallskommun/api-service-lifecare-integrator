package se.sundsvall.lifecareintegrator.integration.familycare.configuration;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyCarePropertiesTest {

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void percentEncodedKeysAreAccepted() {
		final var properties = new FamilyCareProperties("http://fc", "the-domain", "ab%2Bcd%3D", "ef%2Fgh%3D", 5, 30, "NONE");

		assertThat(VALIDATOR.validate(properties)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"ab+cd", "ab/cd", "abcd="
	})
	void decodedKeysAreRejected(final String key) {
		final var properties = new FamilyCareProperties("http://fc", "the-domain", key, key, 5, 30, "NONE");

		assertThat(VALIDATOR.validate(properties))
			.extracting(violation -> violation.getPropertyPath().toString())
			.containsExactlyInAnyOrder("key", "userKey");
	}

	@Test
	void accessors() {
		final var properties = new FamilyCareProperties("http://fc", "the-domain", "the-key", "the-user-key", 5, 30, "FULL");

		assertThat(properties.url()).isEqualTo("http://fc");
		assertThat(properties.domain()).isEqualTo("the-domain");
		assertThat(properties.key()).isEqualTo("the-key");
		assertThat(properties.userKey()).isEqualTo("the-user-key");
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
		assertThat(properties.logLevel()).isEqualTo("FULL");
	}

	@Test
	void getUserKeyOrDefaultUsesTheUserKeyWhenConfigured() {
		final var properties = new FamilyCareProperties("http://fc", "the-domain", "the-key", "the-user-key", 5, 30, "NONE");

		assertThat(properties.getUserKeyOrDefault()).isEqualTo("the-user-key");
	}

	@Test
	void getUserKeyOrDefaultFallsBackToTheMainKeyWhenUnsetOrBlank() {
		final var unset = new FamilyCareProperties("http://fc", "the-domain", "the-key", null, 5, 30, "NONE");
		final var blank = new FamilyCareProperties("http://fc", "the-domain", "the-key", "   ", 5, 30, "NONE");

		assertThat(unset.getUserKeyOrDefault()).isEqualTo("the-key");
		assertThat(blank.getUserKeyOrDefault()).isEqualTo("the-key");
	}
}
