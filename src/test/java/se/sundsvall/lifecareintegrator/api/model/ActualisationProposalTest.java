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

class ActualisationProposalTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ActualisationProposal.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var actualisationTypes = List.of(ActualisationType.create());
		final var specifyTypes = List.of(CodeItem.create());
		final var workingStatus = List.of(CodeItem.create());
		final var organizations = List.of(OrganizationItem.create());
		final var investigations = List.of(ProposalCase.create());
		final var services = List.of(ProposalCase.create());
		final var attachmentTypes = List.of(AttachmentType.create());

		// Act
		final var result = ActualisationProposal.create()
			.withActualisationTypes(actualisationTypes)
			.withSpecifyTypes(specifyTypes)
			.withWorkingStatus(workingStatus)
			.withOrganizations(organizations)
			.withInvestigations(investigations)
			.withServices(services)
			.withAttachmentTypes(attachmentTypes);

		// Assert
		assertThat(result.getActualisationTypes()).isEqualTo(actualisationTypes);
		assertThat(result.getSpecifyTypes()).isEqualTo(specifyTypes);
		assertThat(result.getWorkingStatus()).isEqualTo(workingStatus);
		assertThat(result.getOrganizations()).isEqualTo(organizations);
		assertThat(result.getInvestigations()).isEqualTo(investigations);
		assertThat(result.getServices()).isEqualTo(services);
		assertThat(result.getAttachmentTypes()).isEqualTo(attachmentTypes);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ActualisationProposal.create()).hasAllNullFieldsOrProperties();
	}
}
