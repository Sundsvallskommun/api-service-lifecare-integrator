package se.sundsvall.lifecareintegrator.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.lifecareintegrator.api.model.SourceStatus.STATUS_OK;

class SourceStatusTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(SourceStatus.class, allOf(
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
		final var status = STATUS_OK;

		// Act
		final var result = SourceStatus.create()
			.withSource(source)
			.withLaw(law)
			.withStatus(status);

		// Assert
		assertThat(result.getSource()).isEqualTo(source);
		assertThat(result.getLaw()).isEqualTo(law);
		assertThat(result.getStatus()).isEqualTo(status);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SourceStatus.create()).hasAllNullFieldsOrProperties();
	}
}
