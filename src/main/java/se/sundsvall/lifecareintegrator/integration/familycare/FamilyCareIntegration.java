package se.sundsvall.lifecareintegrator.integration.familycare;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedExecutionDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedInvestigationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedResourceAllocationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedServiceDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedContactDTO;
import generated.se.sundsvall.familycare.PersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.PersonBasedPersonDTO;
import generated.se.sundsvall.familycare.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.familycare.PostCalculationBodyRequest;
import generated.se.sundsvall.familycare.User;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Collections.emptyList;
import static se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareDates.atUtc;
import static se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareDates.endOfDay;
import static se.sundsvall.lifecareintegrator.integration.familycare.FamilyCareDates.startOfDay;

/**
 * Wrapper around {@link FamilyCareClient}. Normalizes the FC quirks: the date-window reads take {@link LocalDate}
 * instead of the RFC 3339 date-times FC insists on (see {@link FamilyCareDates}), and the unified decision fetch pages
 * through all result pages.
 */
@Component
public class FamilyCareIntegration {

	static final int DECISION_PAGE_SIZE = 100;

	/** FamilyCare's first page. Its contract: "The page number you want to retrieve. Default is 0 for the first page". */
	static final int FIRST_PAGE = 0;

	private final FamilyCareClient familyCareClient;

	public FamilyCareIntegration(final FamilyCareClient familyCareClient) {
		this.familyCareClient = familyCareClient;
	}

	public PersonBasedPersonDTO getPerson(final String personNumber) {
		return familyCareClient.getPerson(personNumber);
	}

	public List<PersonBasedContactDTO> getContacts(final String personNumber) {
		return Optional.ofNullable(familyCareClient.getContacts(personNumber))
			.orElse(emptyList());
	}

	/**
	 * Fetch ALL decisions for a person within the given window, paging through every result page. Used by the unified
	 * decisions endpoint, which merges sources and therefore cannot expose FC's native paging.
	 */
	public List<PersonBasedDecisionDTO> getAllDecisions(final String personNumber, final LocalDate startDate, final LocalDate endDate) {
		final var decisions = new ArrayList<PersonBasedDecisionDTO>();
		var pageNr = FIRST_PAGE;
		Integer totalPages;
		do {
			final var result = familyCareClient.getDecisions(personNumber, startOfDay(startDate), endOfDay(endDate), DECISION_PAGE_SIZE, pageNr, null);
			if (result == null) {
				break;
			}
			decisions.addAll(result.getResult());
			totalPages = result.getTotalNumberOfPages();
			pageNr++;
		} while (totalPages != null && pageNr < totalPages);
		return decisions;
	}

	public ApiPaginationCompositePersonBasedAktualiseringDTO getActualisations(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getActualisation(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedCalculationDTO getCalculations(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getCalculations(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedPaymentDTO getPayments(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getPayments(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedInvestigationDTO getInvestigations(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getInvestigations(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedServiceDTO getServices(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getServices(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedExecutionDTO getExecutions(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getExecutions(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedResourceAllocationDTO getResourceAllocations(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getResourceAllocations(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public ApiPaginationCompositePersonBasedDocumentDTO getDocuments(final String personNumber, final LocalDate startDate, final LocalDate endDate,
		final Integer pageSize, final Integer page, final Boolean ascending) {
		return familyCareClient.getDocuments(personNumber, startOfDay(startDate), endOfDay(endDate), pageSize, toFcPageNr(page), ascending);
	}

	public byte[] getDocumentContent(final String documentId) {
		return familyCareClient.getDocumentContent(documentId);
	}

	public List<User> getUsers(final Integer limit, final Integer offset, final OffsetDateTime modifiedAfter, final OffsetDateTime modifiedBefore) {
		return Optional.ofNullable(familyCareClient.getUsers(limit, offset, atUtc(modifiedAfter), atUtc(modifiedBefore)))
			.orElse(emptyList());
	}

	public PersonBasedAktualiseringProposalDTO getActualisationProposal(final String personNumber) {
		return familyCareClient.getActualisationProposal(personNumber);
	}

	public Integer createActualisation(final PostAktualiseringsBodyRequest body) {
		return familyCareClient.createActualisation(body);
	}

	public PersonBasedCalculationProposalDTO getCalculationProposal(final String personNumber) {
		return familyCareClient.getCalculationProposal(personNumber);
	}

	public Integer createCalculation(final PostCalculationBodyRequest body) {
		return familyCareClient.createCalculation(body);
	}

	public void addActualisationAttachment(final Integer actualisationId, final String documentType, final String senderType,
		final String title, final String senderName, final MultipartFile content) {
		familyCareClient.postActualisationAttachment(actualisationId, documentType, senderType, title, senderName, content);
	}

	/** FamilyCare counts pages from 0, this service from 1. */
	private static int toFcPageNr(final int page) {
		return page - 1;
	}
}
