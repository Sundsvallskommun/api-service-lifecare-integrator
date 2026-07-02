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

class ResourceAllocationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ResourceAllocation.class, allOf(
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
		final var startDate = LocalDate.now();
		final var endDate = LocalDate.now().plusDays(1);
		final var percent = 100;
		final var resource = "Anna Andersson";
		final var resourceType = "Handläggare";
		final var serviceId = 23456;

		// Act
		final var result = ResourceAllocation.create()
			.withId(id)
			.withStartDate(startDate)
			.withEndDate(endDate)
			.withPercent(percent)
			.withResource(resource)
			.withResourceType(resourceType)
			.withServiceId(serviceId);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getStartDate()).isEqualTo(startDate);
		assertThat(result.getEndDate()).isEqualTo(endDate);
		assertThat(result.getPercent()).isEqualTo(percent);
		assertThat(result.getResource()).isEqualTo(resource);
		assertThat(result.getResourceType()).isEqualTo(resourceType);
		assertThat(result.getServiceId()).isEqualTo(serviceId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ResourceAllocation.create()).hasAllNullFieldsOrProperties();
	}
}
