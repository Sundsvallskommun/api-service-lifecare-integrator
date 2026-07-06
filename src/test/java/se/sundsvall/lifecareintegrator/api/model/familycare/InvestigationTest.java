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

class InvestigationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Investigation.class, allOf(
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
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusDays(1);
		final var caseworker = "Anna Andersson";
		final var organization = "IFO Vuxen";
		final var dossierType = "Vuxen";
		final var applicant = "Bengt Bengtsson";
		final var coApplicant = "Cecilia Cedersson";
		final var persons = List.of(InvestigationPerson.create());

		// Act
		final var result = Investigation.create()
			.withId(id)
			.withType(type)
			.withFromDate(fromDate)
			.withToDate(toDate)
			.withCaseworker(caseworker)
			.withOrganization(organization)
			.withDossierType(dossierType)
			.withApplicant(applicant)
			.withCoApplicant(coApplicant)
			.withPersons(persons);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getFromDate()).isEqualTo(fromDate);
		assertThat(result.getToDate()).isEqualTo(toDate);
		assertThat(result.getCaseworker()).isEqualTo(caseworker);
		assertThat(result.getOrganization()).isEqualTo(organization);
		assertThat(result.getDossierType()).isEqualTo(dossierType);
		assertThat(result.getApplicant()).isEqualTo(applicant);
		assertThat(result.getCoApplicant()).isEqualTo(coApplicant);
		assertThat(result.getPersons()).isEqualTo(persons);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Investigation.create()).hasAllNullFieldsOrProperties();
	}
}
