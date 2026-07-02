package se.sundsvall.lifecareintegrator.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;

@Schema(description = "A proposal with the valid values needed to create a calculation in Lifecare FC")
public class CalculationProposal {

	@Schema(description = "The investigations that can be linked to")
	private List<ProposalCase> investigations;

	@Schema(description = "The services that can be linked to")
	private List<ProposalCase> services;

	@Schema(description = "The norms a calculation can be based on")
	private List<Norm> norms;

	@Schema(description = "The members of the household")
	private List<HouseholdMember> householdMembers;

	@Schema(description = "The valid income types")
	private List<CodeItem> incomeTypes;

	@Schema(description = "The valid expense types")
	private List<CodeItem> expenseTypes;

	@Schema(description = "The valid special expense types")
	private List<CodeItem> specialExpenseTypes;

	@Schema(description = "Whether a calculation must be linked to an actualisation", examples = "true")
	private Boolean actualisationMandatory;

	@Schema(description = "The number of family members not in the household", examples = "0")
	private Integer numberOfFamilyMembersNotInHousehold;

	@Schema(description = "The existing actualisations that a calculation can be linked to")
	private List<ActualisationItem> actualisations;

	public static CalculationProposal create() {
		return new CalculationProposal();
	}

	public List<ProposalCase> getInvestigations() {
		return investigations;
	}

	public void setInvestigations(final List<ProposalCase> investigations) {
		this.investigations = investigations;
	}

	public CalculationProposal withInvestigations(final List<ProposalCase> investigations) {
		this.investigations = investigations;
		return this;
	}

	public List<ProposalCase> getServices() {
		return services;
	}

	public void setServices(final List<ProposalCase> services) {
		this.services = services;
	}

	public CalculationProposal withServices(final List<ProposalCase> services) {
		this.services = services;
		return this;
	}

	public List<Norm> getNorms() {
		return norms;
	}

	public void setNorms(final List<Norm> norms) {
		this.norms = norms;
	}

	public CalculationProposal withNorms(final List<Norm> norms) {
		this.norms = norms;
		return this;
	}

	public List<HouseholdMember> getHouseholdMembers() {
		return householdMembers;
	}

	public void setHouseholdMembers(final List<HouseholdMember> householdMembers) {
		this.householdMembers = householdMembers;
	}

	public CalculationProposal withHouseholdMembers(final List<HouseholdMember> householdMembers) {
		this.householdMembers = householdMembers;
		return this;
	}

	public List<CodeItem> getIncomeTypes() {
		return incomeTypes;
	}

	public void setIncomeTypes(final List<CodeItem> incomeTypes) {
		this.incomeTypes = incomeTypes;
	}

	public CalculationProposal withIncomeTypes(final List<CodeItem> incomeTypes) {
		this.incomeTypes = incomeTypes;
		return this;
	}

	public List<CodeItem> getExpenseTypes() {
		return expenseTypes;
	}

	public void setExpenseTypes(final List<CodeItem> expenseTypes) {
		this.expenseTypes = expenseTypes;
	}

	public CalculationProposal withExpenseTypes(final List<CodeItem> expenseTypes) {
		this.expenseTypes = expenseTypes;
		return this;
	}

	public List<CodeItem> getSpecialExpenseTypes() {
		return specialExpenseTypes;
	}

	public void setSpecialExpenseTypes(final List<CodeItem> specialExpenseTypes) {
		this.specialExpenseTypes = specialExpenseTypes;
	}

	public CalculationProposal withSpecialExpenseTypes(final List<CodeItem> specialExpenseTypes) {
		this.specialExpenseTypes = specialExpenseTypes;
		return this;
	}

	public Boolean getActualisationMandatory() {
		return actualisationMandatory;
	}

	public void setActualisationMandatory(final Boolean actualisationMandatory) {
		this.actualisationMandatory = actualisationMandatory;
	}

	public CalculationProposal withActualisationMandatory(final Boolean actualisationMandatory) {
		this.actualisationMandatory = actualisationMandatory;
		return this;
	}

	public Integer getNumberOfFamilyMembersNotInHousehold() {
		return numberOfFamilyMembersNotInHousehold;
	}

	public void setNumberOfFamilyMembersNotInHousehold(final Integer numberOfFamilyMembersNotInHousehold) {
		this.numberOfFamilyMembersNotInHousehold = numberOfFamilyMembersNotInHousehold;
	}

	public CalculationProposal withNumberOfFamilyMembersNotInHousehold(final Integer numberOfFamilyMembersNotInHousehold) {
		this.numberOfFamilyMembersNotInHousehold = numberOfFamilyMembersNotInHousehold;
		return this;
	}

	public List<ActualisationItem> getActualisations() {
		return actualisations;
	}

	public void setActualisations(final List<ActualisationItem> actualisations) {
		this.actualisations = actualisations;
	}

	public CalculationProposal withActualisations(final List<ActualisationItem> actualisations) {
		this.actualisations = actualisations;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CalculationProposal that = (CalculationProposal) o;
		return Objects.equals(investigations, that.investigations) && Objects.equals(services, that.services) && Objects.equals(norms, that.norms)
			&& Objects.equals(householdMembers, that.householdMembers) && Objects.equals(incomeTypes, that.incomeTypes) && Objects.equals(expenseTypes, that.expenseTypes)
			&& Objects.equals(specialExpenseTypes, that.specialExpenseTypes) && Objects.equals(actualisationMandatory, that.actualisationMandatory)
			&& Objects.equals(numberOfFamilyMembersNotInHousehold, that.numberOfFamilyMembersNotInHousehold) && Objects.equals(actualisations, that.actualisations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(investigations, services, norms, householdMembers, incomeTypes, expenseTypes, specialExpenseTypes, actualisationMandatory,
			numberOfFamilyMembersNotInHousehold, actualisations);
	}

	@Override
	public String toString() {
		return "CalculationProposal{" +
			"investigations=" + investigations +
			", services=" + services +
			", norms=" + norms +
			", householdMembers=" + householdMembers +
			", incomeTypes=" + incomeTypes +
			", expenseTypes=" + expenseTypes +
			", specialExpenseTypes=" + specialExpenseTypes +
			", actualisationMandatory=" + actualisationMandatory +
			", numberOfFamilyMembersNotInHousehold=" + numberOfFamilyMembersNotInHousehold +
			", actualisations=" + actualisations +
			'}';
	}
}
