package se.sundsvall.lifecareintegrator.api.model.familycare;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.api.model.common.Lookup;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ActualisationTypeTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ActualisationType.class, allOf(
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
		final var specifyTypeMandatory = true;
		final var workingStatus = false;
		final var reasons = List.of(Lookup.create());
		final var fromWho = List.of(Lookup.create());
		final var investigationTypes = List.of(Lookup.create());
		final var serviceTypes = List.of(Lookup.create());

		// Act
		final var result = ActualisationType.create()
			.withId(id)
			.withName(name)
			.withSpecifyTypeMandatory(specifyTypeMandatory)
			.withWorkingStatus(workingStatus)
			.withReasons(reasons)
			.withFromWho(fromWho)
			.withInvestigationTypes(investigationTypes)
			.withServiceTypes(serviceTypes);

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getSpecifyTypeMandatory()).isEqualTo(specifyTypeMandatory);
		assertThat(result.getWorkingStatus()).isEqualTo(workingStatus);
		assertThat(result.getReasons()).isEqualTo(reasons);
		assertThat(result.getFromWho()).isEqualTo(fromWho);
		assertThat(result.getInvestigationTypes()).isEqualTo(investigationTypes);
		assertThat(result.getServiceTypes()).isEqualTo(serviceTypes);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ActualisationType.create()).hasAllNullFieldsOrProperties();
	}
}
