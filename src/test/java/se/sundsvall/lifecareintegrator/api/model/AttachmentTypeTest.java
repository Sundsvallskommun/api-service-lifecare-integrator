package se.sundsvall.lifecareintegrator.api.model;

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

class AttachmentTypeTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AttachmentType.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = 1;
		final var name = "Ansökan";
		final var senderTypes = List.of(CodeItem.create());

		// Act
		final var result = AttachmentType.create()
			.withId(id)
			.withName(name)
			.withSenderTypes(senderTypes);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getSenderTypes()).isEqualTo(senderTypes);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AttachmentType.create()).hasAllNullFieldsOrProperties();
	}
}
