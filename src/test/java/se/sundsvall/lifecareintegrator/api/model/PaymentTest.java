package se.sundsvall.lifecareintegrator.api.model;

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

class PaymentTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Payment.class, allOf(
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
		final var amount = 5000.0;
		final var paymentMethod = "Bankkonto";
		final var payDate = LocalDate.now();
		final var clearing = "8420";
		final var accountNumber = "1234567";
		final var name = "Anna Andersson";
		final var streetAddress = "Storgatan 1";
		final var careOfAddress = "c/o Andersson";
		final var postalCode = "85230";
		final var postalAddress = "Sundsvall";
		final var billingNumber = "123-4567";
		final var localNumber = "42";
		final var voucherNumber = "V12345";
		final var message = "Ekonomiskt bistånd maj";
		final var investigationExecutionId = 23456;
		final var serviceId = 34567;
		final var connectedApplication = 45678;
		final var concernedMonth = "2026-05";
		final var persons = List.of("Anna Andersson");

		// Act
		final var result = Payment.create()
			.withId(id)
			.withAmount(amount)
			.withPaymentMethod(paymentMethod)
			.withPayDate(payDate)
			.withClearing(clearing)
			.withAccountNumber(accountNumber)
			.withName(name)
			.withStreetAddress(streetAddress)
			.withCareOfAddress(careOfAddress)
			.withPostalCode(postalCode)
			.withPostalAddress(postalAddress)
			.withBillingNumber(billingNumber)
			.withLocalNumber(localNumber)
			.withVoucherNumber(voucherNumber)
			.withMessage(message)
			.withInvestigationExecutionId(investigationExecutionId)
			.withServiceId(serviceId)
			.withConnectedApplication(connectedApplication)
			.withConcernedMonth(concernedMonth)
			.withPersons(persons);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getAmount()).isEqualTo(amount);
		assertThat(result.getPaymentMethod()).isEqualTo(paymentMethod);
		assertThat(result.getPayDate()).isEqualTo(payDate);
		assertThat(result.getClearing()).isEqualTo(clearing);
		assertThat(result.getAccountNumber()).isEqualTo(accountNumber);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getStreetAddress()).isEqualTo(streetAddress);
		assertThat(result.getCareOfAddress()).isEqualTo(careOfAddress);
		assertThat(result.getPostalCode()).isEqualTo(postalCode);
		assertThat(result.getPostalAddress()).isEqualTo(postalAddress);
		assertThat(result.getBillingNumber()).isEqualTo(billingNumber);
		assertThat(result.getLocalNumber()).isEqualTo(localNumber);
		assertThat(result.getVoucherNumber()).isEqualTo(voucherNumber);
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getInvestigationExecutionId()).isEqualTo(investigationExecutionId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getConnectedApplication()).isEqualTo(connectedApplication);
		assertThat(result.getConcernedMonth()).isEqualTo(concernedMonth);
		assertThat(result.getPersons()).isEqualTo(persons);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Payment.create()).hasAllNullFieldsOrProperties();
	}
}
