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

class CalculationIncomeRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationIncomeRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var typeId = 1;
		final var applicantAmount = 5000.0;
		final var applicantAmountDate = LocalDate.now();
		final var coApplicantAmount = 5000.0;
		final var coApplicantAmountDate = LocalDate.now().plusDays(1);
		final var note = "Lön april";

		// Act
		final var result = CalculationIncomeRequest.create()
			.withTypeId(typeId)
			.withApplicantAmount(applicantAmount)
			.withApplicantAmountDate(applicantAmountDate)
			.withCoApplicantAmount(coApplicantAmount)
			.withCoApplicantAmountDate(coApplicantAmountDate)
			.withNote(note);

		// Assert
		assertThat(result.getTypeId()).isEqualTo(typeId);
		assertThat(result.getApplicantAmount()).isEqualTo(applicantAmount);
		assertThat(result.getApplicantAmountDate()).isEqualTo(applicantAmountDate);
		assertThat(result.getCoApplicantAmount()).isEqualTo(coApplicantAmount);
		assertThat(result.getCoApplicantAmountDate()).isEqualTo(coApplicantAmountDate);
		assertThat(result.getNote()).isEqualTo(note);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationIncomeRequest.create()).hasAllNullFieldsOrProperties();
	}
}
