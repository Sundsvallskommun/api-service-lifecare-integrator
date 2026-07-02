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

class CreateActualisationRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CreateActualisationRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";
		final var date = LocalDate.now();
		final var typeId = 1;
		final var fromWhoId = 2;
		final var reasonId = 3;
		final var organisationId = 4;
		final var organisationUnitId = "100";
		final var caseworkerId = "abc123";
		final var specifiesId = 5;
		final var serviceId = 12345;
		final var investigationId = 23456;
		final var workingStatusId = 6;

		// Act
		final var result = CreateActualisationRequest.create()
			.withPartyId(partyId)
			.withDate(date)
			.withTypeId(typeId)
			.withFromWhoId(fromWhoId)
			.withReasonId(reasonId)
			.withOrganisationId(organisationId)
			.withOrganisationUnitId(organisationUnitId)
			.withCaseworkerId(caseworkerId)
			.withSpecifiesId(specifiesId)
			.withServiceId(serviceId)
			.withInvestigationId(investigationId)
			.withWorkingStatusId(workingStatusId);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getPartyId()).isEqualTo(partyId);
		assertThat(result.getDate()).isEqualTo(date);
		assertThat(result.getTypeId()).isEqualTo(typeId);
		assertThat(result.getFromWhoId()).isEqualTo(fromWhoId);
		assertThat(result.getReasonId()).isEqualTo(reasonId);
		assertThat(result.getOrganisationId()).isEqualTo(organisationId);
		assertThat(result.getOrganisationUnitId()).isEqualTo(organisationUnitId);
		assertThat(result.getCaseworkerId()).isEqualTo(caseworkerId);
		assertThat(result.getSpecifiesId()).isEqualTo(specifiesId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getWorkingStatusId()).isEqualTo(workingStatusId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CreateActualisationRequest.create()).hasAllNullFieldsOrProperties();
	}
}
