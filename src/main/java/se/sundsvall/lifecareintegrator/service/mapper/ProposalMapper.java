package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationHouseholdMemberDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationProposalDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.sundsvall.lifecareintegrator.api.model.common.Lookup;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationReference;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationType;
import se.sundsvall.lifecareintegrator.api.model.familycare.AttachmentType;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.HouseholdMember;
import se.sundsvall.lifecareintegrator.api.model.familycare.Norm;
import se.sundsvall.lifecareintegrator.api.model.familycare.Organization;
import se.sundsvall.lifecareintegrator.api.model.familycare.ProposalCase;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toPartyId;

/**
 * Maps the FC proposal lookups (the valid code lists used to build actualisation/calculation create requests) to the
 * public proposal models. Household members are exposed with partyId instead of person number — the swap map is
 * provided by the service.
 */
public final class ProposalMapper {

	private ProposalMapper() {}

	public static ActualisationProposal toActualisationProposal(final PersonBasedAktualiseringProposalDTO proposal) {
		return Optional.ofNullable(proposal)
			.map(source -> ActualisationProposal.create()
				.withActualisationTypes(Optional.ofNullable(source.getActualisationTypes()).orElse(emptyList()).stream()
					.map(type -> ActualisationType.create()
						.withId(type.getId())
						.withName(type.getName())
						.withSpecifyTypeMandatory(type.getSpecifyTypeMandatory())
						.withWorkingStatus(type.getWorkingStatus())
						.withReasons(Optional.ofNullable(type.getReasons()).orElse(emptyList()).stream()
							.map(reason -> toLookup(reason.getId(), reason.getName()))
							.toList())
						.withFromWho(Optional.ofNullable(type.getFromWho()).orElse(emptyList()).stream()
							.map(fromWho -> toLookup(fromWho.getId(), fromWho.getName()))
							.toList())
						.withInvestigationTypes(Optional.ofNullable(type.getInvestigationTypes()).orElse(emptyList()).stream()
							.map(investigationType -> toLookup(investigationType.getId(), investigationType.getName()))
							.toList())
						.withServiceTypes(Optional.ofNullable(type.getServiceTypes()).orElse(emptyList()).stream()
							.map(serviceType -> toLookup(serviceType.getId(), serviceType.getName()))
							.toList()))
					.toList())
				.withSpecifyTypes(Optional.ofNullable(source.getSpecifyTypes()).orElse(emptyList()).stream()
					.map(specifyType -> toLookup(specifyType.getId(), specifyType.getName()))
					.toList())
				.withWorkingStatus(Optional.ofNullable(source.getWorkingStatus()).orElse(emptyList()).stream()
					.map(workingStatus -> toLookup(workingStatus.getId(), workingStatus.getName()))
					.toList())
				.withOrganizations(Optional.ofNullable(source.getOrganizations()).orElse(emptyList()).stream()
					.map(organization -> Organization.create()
						.withId(organization.getId())
						.withUnitId(organization.getUnitId())
						.withName(organization.getName()))
					.toList())
				.withInvestigations(Optional.ofNullable(source.getInvestigations()).orElse(emptyList()).stream()
					.map(investigation -> ProposalCase.create()
						.withId(investigation.getId())
						.withType(investigation.getType())
						.withName(investigation.getName())
						.withStartDate(toLocalDate(investigation.getStartDate()))
						.withOrganisationId(investigation.getOrganisationId())
						.withOrganisationUnitId(investigation.getOrganisationUnitId())
						.withCaseworkerId(investigation.getCaseworkerId()))
					.toList())
				.withServices(Optional.ofNullable(source.getServices()).orElse(emptyList()).stream()
					.map(service -> ProposalCase.create()
						.withId(service.getId())
						.withType(service.getType())
						.withName(service.getName())
						.withStartDate(toLocalDate(service.getStartDate()))
						.withOrganisationId(service.getOrganisationId())
						.withOrganisationUnitId(service.getOrganisationUnitId())
						.withCaseworkerId(service.getCaseworkerId()))
					.toList())
				.withAttachmentTypes(Optional.ofNullable(source.getAttachmentTypes()).orElse(emptyList()).stream()
					.map(attachmentType -> AttachmentType.create()
						.withId(attachmentType.getId())
						.withName(attachmentType.getName())
						.withSenderTypes(Optional.ofNullable(attachmentType.getSenderTypes()).orElse(emptyList()).stream()
							.map(senderType -> toLookup(senderType.getId(), senderType.getName()))
							.toList()))
					.toList()))
			.orElse(null);
	}

	public static CalculationProposal toCalculationProposal(final PersonBasedCalculationProposalDTO proposal, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(proposal)
			.map(source -> CalculationProposal.create()
				.withInvestigations(Optional.ofNullable(source.getInvestigations()).orElse(emptyList()).stream()
					.map(investigation -> ProposalCase.create()
						.withId(investigation.getId())
						.withType(investigation.getType())
						.withName(investigation.getName())
						.withStartDate(toLocalDate(investigation.getStartDate())))
					.toList())
				.withServices(Optional.ofNullable(source.getServices()).orElse(emptyList()).stream()
					.map(service -> ProposalCase.create()
						.withId(service.getId())
						.withType(service.getType())
						.withName(service.getName())
						.withStartDate(toLocalDate(service.getStartDate())))
					.toList())
				.withNorms(Optional.ofNullable(source.getNorms()).orElse(emptyList()).stream()
					.map(norm -> Norm.create()
						.withId(norm.getId())
						.withName(norm.getName())
						.withValidFrom(toLocalDate(norm.getFromDate()))
						.withValidTo(toLocalDate(norm.getToDate())))
					.toList())
				.withHouseholdMembers(Optional.ofNullable(source.getHouseholdMembers()).orElse(emptyList()).stream()
					.map(member -> HouseholdMember.create()
						.withPartyId(toPartyId(member.getPersonId(), partyIdsByPersonNumber))
						.withName(member.getName())
						.withChildFromOtherHousehold(member.getChildFromOtherHousehold()))
					.toList())
				.withIncomeTypes(Optional.ofNullable(source.getCalculationIncomeTypes()).orElse(emptyList()).stream()
					.map(incomeType -> toLookup(incomeType.getId(), incomeType.getName()))
					.toList())
				.withExpenseTypes(Optional.ofNullable(source.getCalculationExpenseTypes()).orElse(emptyList()).stream()
					.map(expenseType -> toLookup(expenseType.getId(), expenseType.getName()))
					.toList())
				.withSpecialExpenseTypes(Optional.ofNullable(source.getCalculationSpecialExpenseTypes()).orElse(emptyList()).stream()
					.map(specialExpenseType -> toLookup(specialExpenseType.getId(), specialExpenseType.getName()))
					.toList())
				.withActualisationMandatory(source.getAktualiseringMandatory())
				.withNumberOfFamilyMembersNotInHousehold(source.getNumberOfFamilyMembersNotInHousehold())
				.withActualisations(Optional.ofNullable(source.getAktualiserings()).orElse(emptyList()).stream()
					.map(actualisation -> ActualisationReference.create()
						.withId(actualisation.getId())
						.withType(actualisation.getType())
						.withDate(toLocalDate(actualisation.getDate())))
					.toList()))
			.orElse(null);
	}

	public static List<String> collectPersonNumbers(final PersonBasedCalculationProposalDTO proposal) {
		return Optional.ofNullable(proposal)
			.map(PersonBasedCalculationProposalDTO::getHouseholdMembers)
			.orElseGet(List::of).stream()
			.map(PersonBasedCalculationHouseholdMemberDTO::getPersonId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
	}

	private static Lookup toLookup(final Integer id, final String name) {
		return Lookup.create()
			.withId(id)
			.withName(name);
	}

}
