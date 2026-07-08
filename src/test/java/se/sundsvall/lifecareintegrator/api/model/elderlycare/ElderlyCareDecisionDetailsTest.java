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
		final var expected = new ElderlyCareDecisionDetails();
		expected.setInvestigationId(investigationId);
		expected.setCode(code);
		expected.setServiceCategory(serviceCategory);
		expected.setHours(hours);
		expected.setHourType(hourType);
		expected.setAmountType(amountType);
		expected.setQuantity(quantity);
		expected.setQuantityType(quantityType);
		expected.setVisits(visits);
		expected.setVisitType(visitType);
		expected.setDays(days);
		expected.setDayType(dayType);
		expected.setDecisionLevel(decisionLevel);
		expected.setExecutionStartDate(executionStartDate);
		expected.setExecutionEndDate(executionEndDate);
		expected.setIterationNumber(iterationNumber);
		expected.setDaysOfDecision(daysOfDecision);
		expected.setOrderIds(orderIds);
		expected.setDeleted(deleted);
		expected.setPersonCategory1(personCategory1);
		expected.setPersonCategory2(personCategory2);
		expected.setPersonCategory3(personCategory3);
		expected.setPersonCategory3P(personCategory3P);
		expected.setIncreasedHourlyAmount(increasedHourlyAmount);
		expected.setStandardAmount(standardAmount);
		expected.setSfbCaseworker(sfbCaseworker);

		// Assert
		assertThat(result).usingRecursiveComparison().isEqualTo(expected);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ElderlyCareDecisionDetails.create()).hasAllNullFieldsOrProperties();
	}
}
