package se.sundsvall.lifecareintegrator.api.model.elderlycare;

import java.math.BigDecimal;
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

class ElderlyCareDecisionDetailsTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ElderlyCareDecisionDetails.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var investigationId = 5678;
		final var code = "Beviljad";
		final var serviceCategory = "Hemtjänst";
		final var hours = 10.5;
		final var hourType = "Per vecka";
		final var amountType = "Per månad";
		final var quantity = 2.0;
		final var quantityType = "Per dag";
		final var visits = 3.0;
		final var visitType = "Per vecka";
		final var days = 5.0;
		final var dayType = "Per månad";
		final var decisionLevel = "Delegation";
		final var executionStartDate = LocalDate.now();
		final var executionEndDate = LocalDate.now().plusDays(1);
		final var iterationNumber = 1;
		final var daysOfDecision = 184.0;
		final var orderIds = List.of(1, 2, 3);
		final var deleted = false;
		final var personCategory1 = true;
		final var personCategory2 = false;
		final var personCategory3 = false;
		final var personCategory3P = false;
		final var increasedHourlyAmount = BigDecimal.valueOf(150.0);
		final var standardAmount = BigDecimal.valueOf(300.0);
		final var sfbCaseworker = "Bo Bengtsson";

		// Act
		final var result = ElderlyCareDecisionDetails.create()
			.withInvestigationId(investigationId)
			.withCode(code)
			.withServiceCategory(serviceCategory)
			.withHours(hours)
			.withHourType(hourType)
			.withAmountType(amountType)
			.withQuantity(quantity)
			.withQuantityType(quantityType)
			.withVisits(visits)
			.withVisitType(visitType)
			.withDays(days)
			.withDayType(dayType)
			.withDecisionLevel(decisionLevel)
			.withExecutionStartDate(executionStartDate)
			.withExecutionEndDate(executionEndDate)
			.withIterationNumber(iterationNumber)
			.withDaysOfDecision(daysOfDecision)
			.withOrderIds(orderIds)
			.withDeleted(deleted)
			.withPersonCategory1(personCategory1)
			.withPersonCategory2(personCategory2)
			.withPersonCategory3(personCategory3)
			.withPersonCategory3P(personCategory3P)
			.withIncreasedHourlyAmount(increasedHourlyAmount)
			.withStandardAmount(standardAmount)
			.withSfbCaseworker(sfbCaseworker);

		// Assert
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getCode()).isEqualTo(code);
		assertThat(result.getServiceCategory()).isEqualTo(serviceCategory);
		assertThat(result.getHours()).isEqualTo(hours);
		assertThat(result.getHourType()).isEqualTo(hourType);
		assertThat(result.getAmountType()).isEqualTo(amountType);
		assertThat(result.getQuantity()).isEqualTo(quantity);
		assertThat(result.getQuantityType()).isEqualTo(quantityType);
		assertThat(result.getVisits()).isEqualTo(visits);
		assertThat(result.getVisitType()).isEqualTo(visitType);
		assertThat(result.getDays()).isEqualTo(days);
		assertThat(result.getDayType()).isEqualTo(dayType);
		assertThat(result.getDecisionLevel()).isEqualTo(decisionLevel);
		assertThat(result.getExecutionStartDate()).isEqualTo(executionStartDate);
		assertThat(result.getExecutionEndDate()).isEqualTo(executionEndDate);
		assertThat(result.getIterationNumber()).isEqualTo(iterationNumber);
		assertThat(result.getDaysOfDecision()).isEqualTo(daysOfDecision);
		assertThat(result.getOrderIds()).isEqualTo(orderIds);
		assertThat(result.getDeleted()).isEqualTo(deleted);
		assertThat(result.getPersonCategory1()).isEqualTo(personCategory1);
		assertThat(result.getPersonCategory2()).isEqualTo(personCategory2);
		assertThat(result.getPersonCategory3()).isEqualTo(personCategory3);
		assertThat(result.getPersonCategory3P()).isEqualTo(personCategory3P);
		assertThat(result.getIncreasedHourlyAmount()).isEqualTo(increasedHourlyAmount);
		assertThat(result.getStandardAmount()).isEqualTo(standardAmount);
		assertThat(result.getSfbCaseworker()).isEqualTo(sfbCaseworker);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ElderlyCareDecisionDetails.create()).hasAllNullFieldsOrProperties();
	}
}
