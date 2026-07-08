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

class ActualisationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Actualisation.class, allOf(
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
		final var type = "Ansökan";
		final var name = "Kalle Karlsson";
		final var date = LocalDate.now();
		final var reason = "Ansökan om ekonomiskt bistånd";
		final var regards = "Ekonomiskt bistånd";
		final var fromWho = "Den enskilde";
		final var caseworker = "Anna Andersson";
		final var organization = "Vuxen försörjningsstöd";
		final var status = "Inledd utredning";
		final var investigationId = 456;
		final var serviceId = 789;
		final var decisionId = 1011;

		// Act
		final var result = Actualisation.create()
			.withId(id)
			.withType(type)
			.withName(name)
			.withDate(date)
			.withReason(reason)
			.withRegards(regards)
			.withFromWho(fromWho)
			.withCaseworker(caseworker)
			.withOrganization(organization)
			.withStatus(status)
			.withInvestigationId(investigationId)
			.withServiceId(serviceId)
			.withDecisionId(decisionId);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getDate()).isEqualTo(date);
		assertThat(result.getReason()).isEqualTo(reason);
		assertThat(result.getRegards()).isEqualTo(regards);
		assertThat(result.getFromWho()).isEqualTo(fromWho);
		assertThat(result.getCaseworker()).isEqualTo(caseworker);
		assertThat(result.getOrganization()).isEqualTo(organization);
		assertThat(result.getStatus()).isEqualTo(status);
		assertThat(result.getInvestigationId()).isEqualTo(investigationId);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
		assertThat(result.getDecisionId()).isEqualTo(decisionId);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Actualisation.create()).hasAllNullFieldsOrProperties();
	}
}
