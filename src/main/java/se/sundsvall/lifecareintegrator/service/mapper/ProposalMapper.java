package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.familycare.PersonBasedAktualiseringAttachmentTypeDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringsInfoDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringsInvestigationDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringsOrganizationDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringsServiceDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationAktualiseringDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationHouseholdMemberDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationInvestigationDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationNormDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationServiceDTO;
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
				.withActualisationTypes(toActualisationTypes(source.getActualisationTypes()))
				.withSpecifyTypes(Optional.ofNullable(source.getSpecifyTypes()).orElse(emptyList()).stream()
					.map(specifyType -> toLookup(specifyType.getId(), specifyType.getName()))
					.toList())
				.withWorkingStatus(Optional.ofNullable(source.getWorkingStatus()).orElse(emptyList()).stream()
					.map(workingStatus -> toLookup(workingStatus.getId(), workingStatus.getName()))
					.toList())
				.withOrganizations(toOrganizations(source.getOrganizations()))
				.withInvestigations(toActualisationInvestigations(source.getInvestigations()))
				.withServices(toActualisationServices(source.getServices()))
				.withAttachmentTypes(toAttachmentTypes(source.getAttachmentTypes())))
			.orElse(null);
	}

	public static CalculationProposal toCalculationProposal(final PersonBasedCalculationProposalDTO proposal, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(proposal)
			.map(source -> CalculationProposal.create()
				.withInvestigations(toCalculationInvestigations(source.getInvestigations()))
				.withServices(toCalculationServices(source.getServices()))
				.withNorms(toNorms(source.getNorms()))
				.withHouseholdMembers(toHouseholdMembers(source.getHouseholdMembers(), partyIdsByPersonNumber))
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
				.withActualisations(toActualisationReferences(source.getAktualiserings())))
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

	private static List<ActualisationType> toActualisationTypes(final List<PersonBasedAktualiseringsInfoDTO> types) {
		return Optional.ofNullable(types).orElse(emptyList()).stream()
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
			.toList();
	}

	private static List<Organization> toOrganizations(final List<PersonBasedAktualiseringsOrganizationDTO> organizations) {
		return Optional.ofNullable(organizations).orElse(emptyList()).stream()
			.map(organization -> Organization.create()
				.withId(organization.getId())
				.withUnitId(organization.getUnitId())
				.withName(organization.getName()))
			.toList();
	}

	private static List<ProposalCase> toActualisationInvestigations(final List<PersonBasedAktualiseringsInvestigationDTO> investigations) {
		return Optional.ofNullable(investigations).orElse(emptyList()).stream()
			.map(investigation -> ProposalCase.create()
				.withId(investigation.getId())
				.withType(investigation.getType())
				.withName(investigation.getName())
				.withStartDate(toLocalDate(investigation.getStartDate()))
				.withOrganisationId(investigation.getOrganisationId())
				.withOrganisationUnitId(investigation.getOrganisationUnitId())
				.withCaseworkerId(investigation.getCaseworkerId()))
			.toList();
	}

	private static List<ProposalCase> toActualisationServices(final List<PersonBasedAktualiseringsServiceDTO> services) {
		return Optional.ofNullable(services).orElse(emptyList()).stream()
			.map(service -> ProposalCase.create()
				.withId(service.getId())
				.withType(service.getType())
				.withName(service.getName())
				.withStartDate(toLocalDate(service.getStartDate()))
				.withOrganisationId(service.getOrganisationId())
				.withOrganisationUnitId(service.getOrganisationUnitId())
				.withCaseworkerId(service.getCaseworkerId()))
			.toList();
	}

	private static List<AttachmentType> toAttachmentTypes(final List<PersonBasedAktualiseringAttachmentTypeDTO> attachmentTypes) {
		return Optional.ofNullable(attachmentTypes).orElse(emptyList()).stream()
			.map(attachmentType -> AttachmentType.create()
				.withId(attachmentType.getId())
				.withName(attachmentType.getName())
				.withSenderTypes(Optional.ofNullable(attachmentType.getSenderTypes()).orElse(emptyList()).stream()
					.map(senderType -> toLookup(senderType.getId(), senderType.getName()))
					.toList()))
			.toList();
	}

	private static List<ProposalCase> toCalculationInvestigations(final List<PersonBasedCalculationInvestigationDTO> investigations) {
		return Optional.ofNullable(investigations).orElse(emptyList()).stream()
			.map(investigation -> ProposalCase.create()
				.withId(investigation.getId())
				.withType(investigation.getType())
				.withName(investigation.getName())
				.withStartDate(toLocalDate(investigation.getStartDate())))
			.toList();
	}

	private static List<ProposalCase> toCalculationServices(final List<PersonBasedCalculationServiceDTO> services) {
		return Optional.ofNullable(services).orElse(emptyList()).stream()
			.map(service -> ProposalCase.create()
				.withId(service.getId())
				.withType(service.getType())
				.withName(service.getName())
				.withStartDate(toLocalDate(service.getStartDate())))
			.toList();
	}

	private static List<Norm> toNorms(final List<PersonBasedCalculationNormDTO> norms) {
		return Optional.ofNullable(norms).orElse(emptyList()).stream()
			.map(norm -> Norm.create()
				.withId(norm.getId())
				.withName(norm.getName())
				.withValidFrom(toLocalDate(norm.getFromDate()))
				.withValidTo(toLocalDate(norm.getToDate())))
			.toList();
	}

	private static List<HouseholdMember> toHouseholdMembers(final List<PersonBasedCalculationHouseholdMemberDTO> members, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(members).orElse(emptyList()).stream()
			.map(member -> HouseholdMember.create()
				.withPartyId(toPartyId(member.getPersonId(), partyIdsByPersonNumber))
				.withName(member.getName())
				.withChildFromOtherHousehold(member.getChildFromOtherHousehold()))
			.toList();
	}

	private static List<ActualisationReference> toActualisationReferences(final List<PersonBasedCalculationAktualiseringDTO> actualisations) {
		return Optional.ofNullable(actualisations).orElse(emptyList()).stream()
			.map(actualisation -> ActualisationReference.create()
				.withId(actualisation.getId())
				.withType(actualisation.getType())
				.withDate(toLocalDate(actualisation.getDate())))
			.toList();
	}

	private static Lookup toLookup(final Integer id, final String name) {
		return Lookup.create()
			.withId(id)
			.withName(name);
	}

}
