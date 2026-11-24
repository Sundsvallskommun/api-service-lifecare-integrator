package se.sundsvall.lifecareintegrator.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.lifecareintegrator.api.model.familycare.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.familycare.Caseworker;
import se.sundsvall.lifecareintegrator.api.model.familycare.Contact;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedActualisationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedCalculationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedDocumentResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedExecutionResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedInvestigationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedPaymentResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedResourceAllocationResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PagedServiceResponse;
import se.sundsvall.lifecareintegrator.api.model.familycare.PeriodParameters;
import se.sundsvall.lifecareintegrator.api.model.familycare.Person;
import se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.service.mapper.ActualisationMapper;
import se.sundsvall.lifecareintegrator.service.mapper.CalculationMapper;
import se.sundsvall.lifecareintegrator.service.mapper.CaseDataMapper;
import se.sundsvall.lifecareintegrator.service.mapper.DocumentMapper;
import se.sundsvall.lifecareintegrator.service.mapper.PersonMapper;
import se.sundsvall.lifecareintegrator.service.mapper.ProposalMapper;
import se.sundsvall.lifecareintegrator.service.mapper.RequestMapper;
import se.sundsvall.lifecareintegrator.service.mapper.UserMapper;

/**
 * All single-source family care (FC) operations. Every person-scoped operation resolves the public partyId to a person
 * number first — the person number is forwarded to FC but never exposed in the public API.
 */
@Service
public class FamilyCareService {

	private final PartyIntegration partyIntegration;
	private final FamilyCareIntegration familyCareIntegration;

	public FamilyCareService(final PartyIntegration partyIntegration, final FamilyCareIntegration familyCareIntegration) {
		this.partyIntegration = partyIntegration;
		this.familyCareIntegration = familyCareIntegration;
	}

	// ---- Person master data ------------------------------------------------------------------------------------------

	public Person getPerson(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return PersonMapper.toPerson(familyCareIntegration.getPerson(personNumber));
	}

	public List<Contact> getContacts(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return PersonMapper.toContacts(familyCareIntegration.getContacts(personNumber));
	}

	// ---- Period reads ------------------------------------------------------------------------------------------------

	public PagedActualisationResponse getActualisations(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return ActualisationMapper.toActualisations(familyCareIntegration.getActualisations(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public PagedCalculationResponse getCalculations(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		final var calculations = familyCareIntegration.getCalculations(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending());
		final var partyIdsByPersonNumber = partyIntegration.getPartyIds(municipalityId, CalculationMapper.collectPersonNumbers(calculations));
		return CalculationMapper.toCalculations(calculations, partyIdsByPersonNumber);
	}

	public PagedPaymentResponse getPayments(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return CaseDataMapper.toPayments(familyCareIntegration.getPayments(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public PagedInvestigationResponse getInvestigations(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return CaseDataMapper.toInvestigations(familyCareIntegration.getInvestigations(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public PagedServiceResponse getServices(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return CaseDataMapper.toCaseServices(familyCareIntegration.getServices(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public PagedExecutionResponse getExecutions(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return CaseDataMapper.toExecutions(familyCareIntegration.getExecutions(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public PagedResourceAllocationResponse getResourceAllocations(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return CaseDataMapper.toResourceAllocations(familyCareIntegration.getResourceAllocations(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	// ---- Documents ---------------------------------------------------------------------------------------------------

	public PagedDocumentResponse getDocuments(final String municipalityId, final PeriodParameters parameters) {
		final var window = DateWindow.of(parameters.getFrom(), parameters.getTo());
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, parameters.getPartyId());
		return DocumentMapper.toDocuments(familyCareIntegration.getDocuments(personNumber, window.start(), window.end(), parameters.getLimit(), parameters.getPage(), parameters.getAscending()));
	}

	public byte[] getDocumentContent(final String documentId) {
		return familyCareIntegration.getDocumentContent(documentId);
	}

	// ---- Users -------------------------------------------------------------------------------------------------------

	public List<Caseworker> getUsers(final Integer limit, final Integer offset,
		final OffsetDateTime modifiedAfter, final OffsetDateTime modifiedBefore) {
		return UserMapper.toCaseworkers(familyCareIntegration.getUsers(limit, offset, modifiedAfter, modifiedBefore));
	}

	// ---- Proposals and write-back ------------------------------------------------------------------------------------

	public ActualisationProposal getActualisationProposal(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return ProposalMapper.toActualisationProposal(familyCareIntegration.getActualisationProposal(personNumber));
	}

	public Integer createActualisation(final String municipalityId, final CreateActualisationRequest request) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, request.getPartyId());
		return familyCareIntegration.createActualisation(RequestMapper.toPostAktualiseringsBodyRequest(request, personNumber));
	}

	public CalculationProposal getCalculationProposal(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		final var proposal = familyCareIntegration.getCalculationProposal(personNumber);
		final var partyIdsByPersonNumber = partyIntegration.getPartyIds(municipalityId, ProposalMapper.collectPersonNumbers(proposal));
		return ProposalMapper.toCalculationProposal(proposal, partyIdsByPersonNumber);
	}

	public Integer createCalculation(final String municipalityId, final CreateCalculationRequest request) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, request.getPartyId());

		// Resolve all household member partyIds in one batch — all must resolve for the calculation to be valid
		final var memberPartyIds = Optional.ofNullable(request.getPersons())
			.map(persons -> persons.stream()
				.map(CalculationPersonRequest::getPartyId)
				.distinct()
				.toList())
			.orElse(List.of());
		final var personNumbersByPartyId = partyIntegration.getPersonNumbers(municipalityId, memberPartyIds);

		return familyCareIntegration.createCalculation(RequestMapper.toPostCalculationBodyRequest(request, personNumber, personNumbersByPartyId));
	}

	public void addActualisationAttachment(final Integer actualisationId, final String documentType,
		final String senderType, final String title, final String senderName, final MultipartFile content) {
		familyCareIntegration.addActualisationAttachment(actualisationId, documentType, senderType, title, senderName, content);
	}
}
