package se.sundsvall.lifecareintegrator.integration.lifecarefc;

import generated.se.sundsvall.lifecarefc.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.lifecareintegrator.Application;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static java.time.Month.MARCH;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Deserializes a {@code Users/GetUsers} response with the application's own Jackson configuration.
 *
 * <p>
 * The payload mirrors what FC served on 2026-09-22, field for field, but every value is invented: the real response
 * carries personnummer and named employees, and those do not belong in a fixture. The two things that matter are
 * shapes, not values — {@code LogonTypes} arrives as ordinals ({@code [1]}) and {@code ValidFrom} arrives without a
 * zone offset.
 *
 * <p>
 * The vendor spec declares {@code LogonTypes} as a string enum (Certificate, External, Idp, Token, Tss), so the
 * generated enum's {@code fromValue} threw {@code Unexpected value '1'} and took the whole response with it — every
 * caseworker lookup failed with a 500, which in careM degrades silently to "no caseworker found" and leaves the
 * errand unassigned. The spec is widened to integers on purpose; this test is what stops it being narrowed back.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class LifecareFcUserDeserializationTest {

	private static final String RESPONSE = """
		[
		  {
		    "Id": "test1",
		    "PersonId": "199001019999",
		    "HsaId": "",
		    "NetworkUserId": "",
		    "VrkId": "",
		    "FirstName": "Test",
		    "LastName": "Testsson",
		    "FullName": "Test Testsson",
		    "Description": "Handläggare, ekonomiskt bistånd",
		    "ValidFrom": "2011-03-29T00:00:00",
		    "ValidTo": null,
		    "Disabled": false,
		    "LogonTypes": [
		      1
		    ],
		    "Password": null
		  }
		]
		""";

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void deserializesAUserWhoseLogonTypesAreOrdinals() {
		final var users = jsonMapper.readValue(RESPONSE, new TypeReference<List<User>>() {});

		assertThat(users).singleElement().satisfies(user -> {
			assertThat(user.getId()).isEqualTo("test1");
			assertThat(user.getFullName()).isEqualTo("Test Testsson");
			assertThat(user.getValidFrom()).isEqualTo(LocalDateTime.of(2011, MARCH, 29, 0, 0));
			assertThat(user.getValidTo()).isNull();
			assertThat(user.getLogonTypes()).containsExactly(1);
		});
	}
}
