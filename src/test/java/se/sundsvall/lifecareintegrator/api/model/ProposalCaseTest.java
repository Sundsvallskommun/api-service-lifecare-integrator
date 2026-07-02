package se.sundsvall.lifecareintegrator.api.model;

import java.time.LocalDate;
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

class ProposalCaseTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ProposalCase.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = 12345;
		final var type = 1;
		final var name = "Utredning ekonomiskt bistånd";
		final var startDate = LocalDate.now();
		final var organisationId = 1;
		final var organisationUnitId = "100";
		final var caseworkerId = "abc123";

		// Act
		final var result = ProposalCase.create()
			.withId(id)
			.withType(type)
			.withName(name)
			.withStartDate(startDate)
			.withOrganisationId(organisationId)
			.withOrganisationUnitId(organisationUnitId)
			.withCaseworkerId(caseworkerId);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getStartDate()).isEqualTo(startDate);
		assertThat(result.getOrganisationId()).isEqualTo(organisationId);
		assertThat(result.getOrganisationUnitId()).isEqualTo(organisationUnitId);
		assertThat(result.getCaseworkerId()).isEqualTo(caseworkerId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ProposalCase.create()).hasAllNullFieldsOrProperties();
	}
}
