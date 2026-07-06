package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class PagedResourceAllocationResponseTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> PagingMetaData.create().withPage(new Random().nextInt(1, 1000)), PagingMetaData.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(PagedResourceAllocationResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var resourceAllocations = List.of(ResourceAllocation.create());
		final var metaData = PagingMetaData.create()
			.withPage(1)
			.withLimit(20)
			.withCount(1)
			.withTotalPages(1)
			.withTotalRecords(1L);

		// Act
		final var result = PagedResourceAllocationResponse.create()
			.withResourceAllocations(resourceAllocations)
			.withMetaData(metaData);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getResourceAllocations()).isEqualTo(resourceAllocations);
		assertThat(result.getMetaData()).isEqualTo(metaData);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(PagedResourceAllocationResponse.create()).hasAllNullFieldsOrProperties();
	}
}
