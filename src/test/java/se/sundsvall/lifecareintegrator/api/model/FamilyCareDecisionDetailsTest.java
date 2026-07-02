package se.sundsvall.lifecareintegrator.api.model;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class FamilyCareDecisionDetailsTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(FamilyCareDecisionDetails.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var investigationExecutionId = 5678;
		final var serviceId = 910;
		final var organization = "Vuxen försörjningsstöd";
		final var coApplicant = "Berit Bengtsson";
		final var reasonCoApplicant = "Bifall";
		final var connectedApplication = 1112;
		final var persons = List.of(DecisionPerson.create());

		// Act
		final var result = FamilyCareDecisionDetails.create()
			.withInvestigationExecutionId(investigationExecutionId)
			.withServiceId(serviceId)
			.withOrganization(organization)
			.withCoApplicant(coApplicant)
			.withReasonCoApplicant(reasonCoApplicant)
			.withConnectedApplication(connectedApplication)
			.withPersons(persons);

		// Assert
		assertThat(result.getInvestigationExecutionId()).isEqualTo(investigationExecutionId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getOrganization()).isEqualTo(organization);
		assertThat(result.getCoApplicant()).isEqualTo(coApplicant);
		assertThat(result.getReasonCoApplicant()).isEqualTo(reasonCoApplicant);
		assertThat(result.getConnectedApplication()).isEqualTo(connectedApplication);
		assertThat(result.getPersons()).isEqualTo(persons);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(FamilyCareDecisionDetails.create()).hasAllNullFieldsOrProperties();
	}
}
