package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalWebFormsTest {

	@Test
	void encodeKeepsTheOrderAndRepeatedNames() {
		final var fields = List.of(Map.entry("51_0_2_1", "true"), Map.entry("51_0_2_1", "false"), Map.entry("X-LEGACY-TOKEN", "TOKEN"));

		assertThat(new String(ProfessionalWebForms.encode(fields), UTF_8))
			.isEqualTo("51_0_2_1=true&51_0_2_1=false&X-LEGACY-TOKEN=TOKEN");
	}

	@Test
	void encodeEncodesNamesAndValuesAndAllowsAnEmptyValue() {
		final var fields = List.of(Map.entry("a b&c", "å=ö/+"), Map.entry("empty", ""));

		assertThat(new String(ProfessionalWebForms.encode(fields), UTF_8))
			.isEqualTo("a+b%26c=%C3%A5%3D%C3%B6%2F%2B&empty=");
	}

	@Test
	void encodeOfNothingIsAnEmptyBody() {
		assertThat(ProfessionalWebForms.encode(List.<Map.Entry<String, String>>of())).isEmpty();
	}

	@Test
	void encodeOfCredentialsEscapesSpacesAndAmpersands() {
		final var fields = List.of(Map.entry("uid", "user"), Map.entry("otp", "sec ret&1"));

		assertThat(new String(ProfessionalWebForms.encode(fields), UTF_8)).isEqualTo("uid=user&otp=sec+ret%261");
	}
}
