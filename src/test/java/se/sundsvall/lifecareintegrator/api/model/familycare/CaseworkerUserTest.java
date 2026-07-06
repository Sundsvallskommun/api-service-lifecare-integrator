package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CaseworkerUserTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt(1000)), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseworkerUser.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = "12345";
		final var hsaId = "SE2321000032-1234";
		final var networkUserId = "annand01";
		final var firstName = "Anna";
		final var lastName = "Andersson";
		final var fullName = "Anna Andersson";
		final var description = "Handläggare ekonomiskt bistånd";
		final var validFrom = OffsetDateTime.now();
		final var validTo = OffsetDateTime.now().plusDays(1);
		final var disabled = false;

		// Act
		final var result = CaseworkerUser.create()
			.withId(id)
			.withHsaId(hsaId)
			.withNetworkUserId(networkUserId)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withFullName(fullName)
			.withDescription(description)
			.withValidFrom(validFrom)
			.withValidTo(validTo)
			.withDisabled(disabled);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getHsaId()).isEqualTo(hsaId);
		assertThat(result.getNetworkUserId()).isEqualTo(networkUserId);
		assertThat(result.getFirstName()).isEqualTo(firstName);
		assertThat(result.getLastName()).isEqualTo(lastName);
		assertThat(result.getFullName()).isEqualTo(fullName);
		assertThat(result.getDescription()).isEqualTo(description);
		assertThat(result.getValidFrom()).isEqualTo(validFrom);
		assertThat(result.getValidTo()).isEqualTo(validTo);
		assertThat(result.getDisabled()).isEqualTo(disabled);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseworkerUser.create()).hasAllNullFieldsOrProperties();
	}
}
