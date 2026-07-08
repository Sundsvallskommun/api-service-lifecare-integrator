package se.sundsvall.lifecareintegrator.api.model.common;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class DecisionsResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(DecisionsResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var decisions = List.of(Decision.create());
		final var sources = List.of(SourceStatus.create());

		// Act
		final var result = DecisionsResponse.create()
			.withDecisions(decisions)
			.withSources(sources);

		// Assert
		assertThat(result.getDecisions()).isEqualTo(decisions);
		assertThat(result.getSources()).isEqualTo(sources);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DecisionsResponse.create()).hasAllNullFieldsOrProperties();
	}
}
