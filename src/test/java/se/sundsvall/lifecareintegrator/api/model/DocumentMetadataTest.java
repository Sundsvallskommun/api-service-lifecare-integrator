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

class DocumentMetadataTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt(1000)), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(DocumentMetadata.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var id = "12345";
		final var title = "Utredning ekonomiskt bistånd";
		final var date = LocalDate.now();
		final var documentType = "Utredning";
		final var ownerType = "Person";

		// Act
		final var result = DocumentMetadata.create()
			.withId(id)
			.withTitle(title)
			.withDate(date)
			.withDocumentType(documentType)
			.withOwnerType(ownerType);

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getTitle()).isEqualTo(title);
		assertThat(result.getDate()).isEqualTo(date);
		assertThat(result.getDocumentType()).isEqualTo(documentType);
		assertThat(result.getOwnerType()).isEqualTo(ownerType);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentMetadata.create()).hasAllNullFieldsOrProperties();
	}
}
