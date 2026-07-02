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

class DecisionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Decision.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var source = "ELDERLY_CARE";
		final var law = "SOL";
		final var decisionId = "12345";
		final var decided = LocalDate.now();
		final var validFrom = LocalDate.now().plusDays(1);
		final var validTo = LocalDate.now().plusDays(2);
		final var type = "type";
		final var reason = "reason";
		final var decisionMaker = "decisionMaker";
		final var amount = 5000.0;
		final var elderlyCareDetails = ElderlyCareDecisionDetails.create();
		final var familyCareDetails = FamilyCareDecisionDetails.create();

		// Act
		final var result = Decision.create()
			.withSource(source)
			.withLaw(law)
			.withDecisionId(decisionId)
			.withDecided(decided)
			.withValidFrom(validFrom)
			.withValidTo(validTo)
			.withType(type)
			.withReason(reason)
			.withDecisionMaker(decisionMaker)
			.withAmount(amount)
			.withElderlyCareDetails(elderlyCareDetails)
			.withFamilyCareDetails(familyCareDetails);

		// Assert
		assertThat(result.getSource()).isEqualTo(source);
		assertThat(result.getLaw()).isEqualTo(law);
		assertThat(result.getDecisionId()).isEqualTo(decisionId);
		assertThat(result.getDecided()).isEqualTo(decided);
		assertThat(result.getValidFrom()).isEqualTo(validFrom);
		assertThat(result.getValidTo()).isEqualTo(validTo);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getReason()).isEqualTo(reason);
		assertThat(result.getDecisionMaker()).isEqualTo(decisionMaker);
		assertThat(result.getAmount()).isEqualTo(amount);
		assertThat(result.getElderlyCareDetails()).isEqualTo(elderlyCareDetails);
		assertThat(result.getFamilyCareDetails()).isEqualTo(familyCareDetails);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Decision.create()).hasAllNullFieldsOrProperties();
	}
}
