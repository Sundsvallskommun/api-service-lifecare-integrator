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

class PagedResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(PagedResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var page = 1;
		final var pageSize = 20;
		final var totalPages = 5;
		final var totalRecords = 98;
		final var results = List.of("first", "second");

		// Act
		final var result = PagedResponse.<String>create()
			.withPage(page)
			.withPageSize(pageSize)
			.withTotalPages(totalPages)
			.withTotalRecords(totalRecords)
			.withResults(results);

		// Assert
		assertThat(result.getPage()).isEqualTo(page);
		assertThat(result.getPageSize()).isEqualTo(pageSize);
		assertThat(result.getTotalPages()).isEqualTo(totalPages);
		assertThat(result.getTotalRecords()).isEqualTo(totalRecords);
		assertThat(result.getResults()).isEqualTo(results);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(PagedResponse.create()).hasAllNullFieldsOrProperties();
	}
}
