package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ProfessionalWebResponse} has an array component, so equals/hashCode must compare the body by content
 * (Arrays.equals/hashCode) rather than identity, and toString must never print the body itself, only its length.
 */
class ProfessionalWebResponseTest {

	private static final URI URI_VALUE = URI.create("https://lifecare.sundsvall.se/WESE.FC.ProfessionalWeb/api2/Thing/Get");

	@Test
	void equalsComparesTheBodyByContent() {
		final var headers = headers();
		final var first = new ProfessionalWebResponse(200, headers, "hello".getBytes(StandardCharsets.UTF_8), URI_VALUE);
		final var second = new ProfessionalWebResponse(200, headers, "hello".getBytes(StandardCharsets.UTF_8), URI_VALUE);

		assertThat(first).isEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}

	@Test
	void equalsIsFalseWhenTheBodyDiffers() {
		final var headers = headers();
		final var first = new ProfessionalWebResponse(200, headers, "hello".getBytes(StandardCharsets.UTF_8), URI_VALUE);
		final var second = new ProfessionalWebResponse(200, headers, "world".getBytes(StandardCharsets.UTF_8), URI_VALUE);

		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void toStringNeverPrintsTheBodyOnlyItsLength() {
		final var response = new ProfessionalWebResponse(200, headers(), "a personnummer maybe".getBytes(StandardCharsets.UTF_8), URI_VALUE);

		final var description = response.toString();

		assertThat(description).contains("body.length=20");
		assertThat(description).doesNotContain("a personnummer maybe");
	}

	private static HttpHeaders headers() {
		return HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (name, value) -> true);
	}
}
