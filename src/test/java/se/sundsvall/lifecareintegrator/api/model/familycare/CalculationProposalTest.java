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

class CalculationProposalTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CalculationProposal.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var investigations = List.of(ProposalCase.create());
		final var services = List.of(ProposalCase.create());
		final var norms = List.of(Norm.create());
		final var householdMembers = List.of(HouseholdMember.create());
		final var incomeTypes = List.of(Lookup.create());
		final var expenseTypes = List.of(Lookup.create());
		final var specialExpenseTypes = List.of(Lookup.create());
		final var actualisationMandatory = true;
		final var numberOfFamilyMembersNotInHousehold = 0;
		final var actualisations = List.of(ActualisationReference.create());

		// Act
		final var result = CalculationProposal.create()
			.withInvestigations(investigations)
			.withServices(services)
			.withNorms(norms)
			.withHouseholdMembers(householdMembers)
			.withIncomeTypes(incomeTypes)
			.withExpenseTypes(expenseTypes)
			.withSpecialExpenseTypes(specialExpenseTypes)
			.withActualisationMandatory(actualisationMandatory)
			.withNumberOfFamilyMembersNotInHousehold(numberOfFamilyMembersNotInHousehold)
			.withActualisations(actualisations);

		// Assert
		assertThat(result.getInvestigations()).isEqualTo(investigations);
		assertThat(result.getServices()).isEqualTo(services);
		assertThat(result.getNorms()).isEqualTo(norms);
		assertThat(result.getHouseholdMembers()).isEqualTo(householdMembers);
		assertThat(result.getIncomeTypes()).isEqualTo(incomeTypes);
		assertThat(result.getExpenseTypes()).isEqualTo(expenseTypes);
		assertThat(result.getSpecialExpenseTypes()).isEqualTo(specialExpenseTypes);
		assertThat(result.getActualisationMandatory()).isEqualTo(actualisationMandatory);
		assertThat(result.getNumberOfFamilyMembersNotInHousehold()).isEqualTo(numberOfFamilyMembersNotInHousehold);
		assertThat(result.getActualisations()).isEqualTo(actualisations);
		assertThat(result).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CalculationProposal.create()).hasAllNullFieldsOrProperties();
	}
}
