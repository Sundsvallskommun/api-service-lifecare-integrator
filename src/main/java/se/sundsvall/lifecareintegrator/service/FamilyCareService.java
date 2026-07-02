package se.sundsvall.lifecareintegrator.service;

import generated.se.sundsvall.lifecarefc.PersonBasedCalculationHouseholdMemberDTO;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.api.model.Actualisation;
import se.sundsvall.lifecareintegrator.api.model.ActualisationProposal;
import se.sundsvall.lifecareintegrator.api.model.Calculation;
import se.sundsvall.lifecareintegrator.api.model.CalculationPersonRequest;
import se.sundsvall.lifecareintegrator.api.model.CalculationProposal;
import se.sundsvall.lifecareintegrator.api.model.CaseService;
import se.sundsvall.lifecareintegrator.api.model.CaseworkerUser;
import se.sundsvall.lifecareintegrator.api.model.Contact;
import se.sundsvall.lifecareintegrator.api.model.CreateActualisationRequest;
import se.sundsvall.lifecareintegrator.api.model.CreateCalculationRequest;
import se.sundsvall.lifecareintegrator.api.model.DocumentMetadata;
import se.sundsvall.lifecareintegrator.api.model.Execution;
import se.sundsvall.lifecareintegrator.api.model.Investigation;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;
import se.sundsvall.lifecareintegrator.api.model.Payment;
import se.sundsvall.lifecareintegrator.api.model.Person;
import se.sundsvall.lifecareintegrator.api.model.ResourceAllocation;
import se.sundsvall.lifecareintegrator.integration.lifecarefc.LifecareFcIntegration;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.service.mapper.ActualisationMapper;
import se.sundsvall.lifecareintegrator.service.mapper.CalculationMapper;
import se.sundsvall.lifecareintegrator.service.mapper.CaseDataMapper;
import se.sundsvall.lifecareintegrator.service.mapper.DocumentMapper;
import se.sundsvall.lifecareintegrator.service.mapper.PersonMapper;
import se.sundsvall.lifecareintegrator.service.mapper.ProposalMapper;
import se.sundsvall.lifecareintegrator.service.mapper.RequestMapper;
import se.sundsvall.lifecareintegrator.service.mapper.UserMapper;

import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * All single-source family care (FC) operations. Every person-scoped operation resolves the public partyId to a
 * person number first — the person number is forwarded to FC but never exposed in the public API.
 */
@Service
public class FamilyCareService {

	static final String PERSON_NOT_FOUND = "No person found for partyId '%s'";
	static final String DOCUMENT_CONTENT_NOT_FOUND = "No document content found for document '%s'";

	private final PartyIntegration partyIntegration;
	private final LifecareFcIntegration lifecareFcIntegration;

	public FamilyCareService(final PartyIntegration partyIntegration, final LifecareFcIntegration lifecareFcIntegration) {
		this.partyIntegration = partyIntegration;
		this.lifecareFcIntegration = lifecareFcIntegration;
	}

	// ---- Person master data ------------------------------------------------------------------------------------------

	public Person getPerson(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return lifecareFcIntegration.getPerson(personNumber)
			.map(PersonMapper::toPerson)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, PERSON_NOT_FOUND.formatted(partyId)));
	}

	public List<Contact> getContacts(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return PersonMapper.toContacts(lifecareFcIntegration.getContacts(personNumber));
	}

	// ---- Period reads ------------------------------------------------------------------------------------------------

	public PagedResponse<Actualisation> getActualisations(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return ActualisationMapper.toActualisations(lifecareFcIntegration.getActualisations(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<Calculation> getCalculations(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CalculationMapper.toCalculations(lifecareFcIntegration.getCalculations(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<Payment> getPayments(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CaseDataMapper.toPayments(lifecareFcIntegration.getPayments(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<Investigation> getInvestigations(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CaseDataMapper.toInvestigations(lifecareFcIntegration.getInvestigations(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<CaseService> getServices(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CaseDataMapper.toCaseServices(lifecareFcIntegration.getServices(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<Execution> getExecutions(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CaseDataMapper.toExecutions(lifecareFcIntegration.getExecutions(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public PagedResponse<ResourceAllocation> getResourceAllocations(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return CaseDataMapper.toResourceAllocations(lifecareFcIntegration.getResourceAllocations(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	// ---- Documents ---------------------------------------------------------------------------------------------------

	public PagedResponse<DocumentMetadata> getDocuments(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to,
		final Integer page, final Integer pageSize, final Boolean ascending) {
		final var window = DateWindow.of(from, to);
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return DocumentMapper.toDocuments(lifecareFcIntegration.getDocuments(personNumber, window.start(), window.end(), pageSize, page, ascending));
	}

	public byte[] getDocumentContent(final String municipalityId, final String documentId) {
		return lifecareFcIntegration.getDocumentContent(documentId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DOCUMENT_CONTENT_NOT_FOUND.formatted(documentId)));
	}

	// ---- Users -------------------------------------------------------------------------------------------------------

	public List<CaseworkerUser> getUsers(final String municipalityId, final Integer limit, final Integer offset,
		final OffsetDateTime modifiedAfter, final OffsetDateTime modifiedBefore) {
		return UserMapper.toCaseworkerUsers(lifecareFcIntegration.getUsers(limit, offset, modifiedAfter, modifiedBefore));
	}

	// ---- Proposals and write-back ------------------------------------------------------------------------------------

	public ActualisationProposal getActualisationProposal(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		return ProposalMapper.toActualisationProposal(lifecareFcIntegration.getActualisationProposal(personNumber));
	}

	public Integer createActualisation(final String municipalityId, final CreateActualisationRequest request) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, request.getPartyId());
		return lifecareFcIntegration.createActualisation(RequestMapper.toPostAktualiseringsBodyRequest(request, personNumber));
	}

	public CalculationProposal getCalculationProposal(final String municipalityId, final String partyId) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, partyId);
		final var proposal = lifecareFcIntegration.getCalculationProposal(personNumber);

		// Swap the household members' person numbers for partyIds — best effort, unresolved members keep a null partyId
		final var partyIdsByPersonNumber = Optional.ofNullable(proposal)
			.map(value -> partyIntegration.getPartyIds(municipalityId, value.getHouseholdMembers().stream()
				.map(PersonBasedCalculationHouseholdMemberDTO::getPersonId)
				.filter(Objects::nonNull)
				.distinct()
				.toList()))
			.orElse(emptyMap());

		return ProposalMapper.toCalculationProposal(proposal, partyIdsByPersonNumber);
	}

	public Integer createCalculation(final String municipalityId, final CreateCalculationRequest request) {
		final var personNumber = partyIntegration.getPersonNumber(municipalityId, request.getPartyId());

		// Resolve all household member partyIds in one batch — all must resolve for the calculation to be valid
		final var memberPartyIds = Optional.ofNullable(request.getPersons())
			.map(persons -> persons.stream()
				.map(CalculationPersonRequest::getPartyId)
				.filter(Objects::nonNull)
				.distinct()
				.toList())
			.orElse(List.of());
		final var personNumbersByPartyId = partyIntegration.getPersonNumbers(municipalityId, memberPartyIds);

		return lifecareFcIntegration.createCalculation(RequestMapper.toPostCalculationBodyRequest(request, personNumber, personNumbersByPartyId));
	}

	public void addActualisationAttachment(final String municipalityId, final Integer actualisationId, final String documentType,
		final String senderType, final String title, final String senderName, final MultipartFile content) {
		lifecareFcIntegration.addActualisationAttachment(actualisationId, documentType, senderType, title, senderName, content);
	}
}
