package se.sundsvall.lifecareintegrator.api.model.familycare;

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

class ExecutionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Execution.class, allOf(
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

		// Act
		final var result = Execution.create()
			.withId(id)
			.withType(type)
			.withFromDate(fromDate)
			.withToDate(toDate)
			.withCaseworker(caseworker)
			.withOrganization(organization)
			.withDossierType(dossierType);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getFromDate()).isEqualTo(fromDate);
		assertThat(result.getToDate()).isEqualTo(toDate);
		assertThat(result.getCaseworker()).isEqualTo(caseworker);
		assertThat(result.getOrganization()).isEqualTo(organization);
		assertThat(result.getDossierType()).isEqualTo(dossierType);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Execution.create()).hasAllNullFieldsOrProperties();
	}
}
