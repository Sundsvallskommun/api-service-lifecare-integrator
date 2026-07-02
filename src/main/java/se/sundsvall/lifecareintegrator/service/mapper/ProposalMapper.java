package se.sundsvall.lifecareintegrator.service.mapper;

import generated.se.sundsvall.lifecarefc.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.lifecarefc.PersonBasedCalculationProposalDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import se.sundsvall.lifecareintegrator.api.model.ActualisationItem;
import se.sundsvall.lifecareintegrator.api.model.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.ActualisationType;
import se.sundsvall.lifecareintegrator.api.model.AttachmentType;
import se.sundsvall.lifecareintegrator.api.model.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.CodeItem;
import se.sundsvall.lifecareintegrator.api.model.HouseholdMember;
import se.sundsvall.lifecareintegrator.api.model.Norm;
import se.sundsvall.lifecareintegrator.api.model.OrganizationItem;
import se.sundsvall.lifecareintegrator.api.model.ProposalCase;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.service.mapper.MapperUtil.toLocalDate;

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
				.withActualisationTypes(mapList(source.getActualisationTypes(), type -> ActualisationType.create()
					.withId(type.getId())
					.withName(type.getName())
					.withSpecifyTypeMandatory(type.getSpecifyTypeMandatory())
					.withWorkingStatus(type.getWorkingStatus())
					.withReasons(mapList(type.getReasons(), reason -> toCodeItem(reason.getId(), reason.getName())))
					.withFromWho(mapList(type.getFromWho(), fromWho -> toCodeItem(fromWho.getId(), fromWho.getName())))
					.withInvestigationTypes(mapList(type.getInvestigationTypes(), investigationType -> toCodeItem(investigationType.getId(), investigationType.getName())))
					.withServiceTypes(mapList(type.getServiceTypes(), serviceType -> toCodeItem(serviceType.getId(), serviceType.getName())))))
				.withSpecifyTypes(mapList(source.getSpecifyTypes(), specifyType -> toCodeItem(specifyType.getId(), specifyType.getName())))
				.withWorkingStatus(mapList(source.getWorkingStatus(), workingStatus -> toCodeItem(workingStatus.getId(), workingStatus.getName())))
				.withOrganizations(mapList(source.getOrganizations(), organization -> OrganizationItem.create()
					.withId(organization.getId())
					.withUnitId(organization.getUnitId())
					.withName(organization.getName())))
				.withInvestigations(mapList(source.getInvestigations(), investigation -> ProposalCase.create()
					.withId(investigation.getId())
					.withType(investigation.getType())
					.withName(investigation.getName())
					.withStartDate(toLocalDate(investigation.getStartDate()))
					.withOrganisationId(investigation.getOrganisationId())
					.withOrganisationUnitId(investigation.getOrganisationUnitId())
					.withCaseworkerId(investigation.getCaseworkerId())))
				.withServices(mapList(source.getServices(), service -> ProposalCase.create()
					.withId(service.getId())
					.withType(service.getType())
					.withName(service.getName())
					.withStartDate(toLocalDate(service.getStartDate()))
					.withOrganisationId(service.getOrganisationId())
					.withOrganisationUnitId(service.getOrganisationUnitId())
					.withCaseworkerId(service.getCaseworkerId())))
				.withAttachmentTypes(mapList(source.getAttachmentTypes(), attachmentType -> AttachmentType.create()
					.withId(attachmentType.getId())
					.withName(attachmentType.getName())
					.withSenderTypes(mapList(attachmentType.getSenderTypes(), senderType -> toCodeItem(senderType.getId(), senderType.getName()))))))
			.orElse(null);
	}

	public static CalculationProposal toCalculationProposal(final PersonBasedCalculationProposalDTO proposal, final Map<String, String> partyIdsByPersonNumber) {
		return Optional.ofNullable(proposal)
			.map(source -> CalculationProposal.create()
				.withInvestigations(mapList(source.getInvestigations(), investigation -> ProposalCase.create()
					.withId(investigation.getId())
					.withType(investigation.getType())
					.withName(investigation.getName())
					.withStartDate(toLocalDate(investigation.getStartDate()))))
				.withServices(mapList(source.getServices(), service -> ProposalCase.create()
					.withId(service.getId())
					.withType(service.getType())
					.withName(service.getName())
					.withStartDate(toLocalDate(service.getStartDate()))))
				.withNorms(mapList(source.getNorms(), norm -> Norm.create()
					.withId(norm.getId())
					.withName(norm.getName())
					.withValidFrom(toLocalDate(norm.getFromDate()))
					.withValidTo(toLocalDate(norm.getToDate()))))
				// The household member person number is swapped for a partyId — unresolved members keep a null partyId
				.withHouseholdMembers(mapList(source.getHouseholdMembers(), member -> HouseholdMember.create()
					.withPartyId(Optional.ofNullable(member.getPersonId()).map(partyIdsByPersonNumber::get).orElse(null))
					.withName(member.getName())
					.withChildFromOtherHousehold(member.getChildFromOtherHousehold())))
				.withIncomeTypes(mapList(source.getCalculationIncomeTypes(), incomeType -> toCodeItem(incomeType.getId(), incomeType.getName())))
				.withExpenseTypes(mapList(source.getCalculationExpenseTypes(), expenseType -> toCodeItem(expenseType.getId(), expenseType.getName())))
				.withSpecialExpenseTypes(mapList(source.getCalculationSpecialExpenseTypes(), specialExpenseType -> toCodeItem(specialExpenseType.getId(), specialExpenseType.getName())))
				.withActualisationMandatory(source.getAktualiseringMandatory())
				.withNumberOfFamilyMembersNotInHousehold(source.getNumberOfFamilyMembersNotInHousehold())
				.withActualisations(mapList(source.getAktualiserings(), actualisation -> ActualisationItem.create()
					.withId(actualisation.getId())
					.withType(actualisation.getType())
					.withDate(toLocalDate(actualisation.getDate())))))
			.orElse(null);
	}

	private static CodeItem toCodeItem(final Integer id, final String name) {
		return CodeItem.create()
			.withId(id)
			.withName(name);
	}

	private static <S, T> List<T> mapList(final List<S> source, final Function<S, T> mapper) {
		return Optional.ofNullable(source)
			.map(list -> list.stream()
				.map(mapper)
				.toList())
			.orElse(emptyList());
	}
}
