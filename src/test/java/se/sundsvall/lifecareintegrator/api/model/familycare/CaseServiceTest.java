package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.time.LocalDate;
import java.util.List;
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

class CaseServiceTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseService.class, allOf(
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
		final var type = "Ekonomiskt bistånd";
		final var organization = "IFO Vuxen";
		final var startDate = LocalDate.now();
		final var endDate = LocalDate.now().plusDays(1);
		final var caseworker = "Anna Andersson";
		final var coCaseworker = "Bengt Bengtsson";
		final var investigationId = 23456;
		final var decisionId = 34567;
		final var applicant = "Cecilia Cedersson";
		final var coApplicant = "David Davidsson";
		final var persons = List.of(RelatedPerson.create().withName("Anna Andersson").withCoApplicant(false));

		// Act
		final var result = CaseService.create()
			.withId(id)
			.withType(type)
			.withOrganization(organization)
			.withStartDate(startDate)
			.withEndDate(endDate)
			.withCaseworker(caseworker)
			.withCoCaseworker(coCaseworker)
			.withInvestigationId(investigationId)
			.withDecisionId(decisionId)
			.withApplicant(applicant)
			.withCoApplicant(coApplicant)
			.withPersons(persons);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getOrganization()).isEqualTo(organization);
		assertThat(result.getStartDate()).isEqualTo(startDate);
		assertThat(result.getEndDate()).isEqualTo(endDate);
		assertThat(result.getCaseworker()).isEqualTo(caseworker);
		assertThat(result.getCoCaseworker()).isEqualTo(coCaseworker);
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getDecisionId()).isEqualTo(decisionId);
		assertThat(result.getApplicant()).isEqualTo(applicant);
		assertThat(result.getCoApplicant()).isEqualTo(coApplicant);
		assertThat(result.getPersons()).isEqualTo(persons);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseService.create()).hasAllNullFieldsOrProperties();
	}
}
