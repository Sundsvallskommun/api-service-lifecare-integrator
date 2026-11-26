package se.sundsvall.lifecareintegrator.integration.familycare;

import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedAktualiseringDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedCalculationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDecisionDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedDocumentDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedExecutionDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedInvestigationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedPaymentDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedResourceAllocationDTO;
import generated.se.sundsvall.familycare.ApiPaginationCompositePersonBasedServiceDTO;
import generated.se.sundsvall.familycare.PersonBasedAktualiseringProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedCalculationProposalDTO;
import generated.se.sundsvall.familycare.PersonBasedContactDTO;
import generated.se.sundsvall.familycare.PersonBasedPersonDTO;
import generated.se.sundsvall.familycare.PostAktualiseringsBodyRequest;
import generated.se.sundsvall.familycare.PostCalculationBodyRequest;
import generated.se.sundsvall.familycare.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.lifecareintegrator.integration.familycare.configuration.FamilyCareConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.lifecareintegrator.integration.familycare.configuration.FamilyCareConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.familycare.url}", configuration = FamilyCareConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface FamilyCareClient {

	@GetMapping(path = "/apifc/v1/Persons", produces = APPLICATION_JSON_VALUE)
	PersonBasedPersonDTO getPerson(
		@RequestParam(value = "personId") final String personId);

	@GetMapping(path = "/apifc/v1/Contacts", produces = APPLICATION_JSON_VALUE)
	List<PersonBasedContactDTO> getContacts(
		@RequestParam(value = "personId") final String personId);

	@GetMapping(path = "/apifc/v1/Actualisations", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedAktualiseringDTO getActualisation(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Calculations", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedCalculationDTO getCalculations(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Decisions", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedDecisionDTO getDecisions(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Payments", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedPaymentDTO getPayments(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Investigations", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedInvestigationDTO getInvestigations(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Services", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedServiceDTO getServices(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Executions", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedExecutionDTO getExecutions(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/ResourceAllocations", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedResourceAllocationDTO getResourceAllocations(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/Documents", produces = APPLICATION_JSON_VALUE)
	ApiPaginationCompositePersonBasedDocumentDTO getDocuments(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "startDate") final String startDate,
		@RequestParam(value = "endDate") final String endDate,
		@RequestParam(value = "pageSize", required = false) final Integer pageSize,
		@RequestParam(value = "pageNr", required = false) final Integer pageNr,
		@RequestParam(value = "ascending", required = false) final Boolean ascending);

	@GetMapping(path = "/apifc/v1/DocumentContent", produces = APPLICATION_PDF_VALUE)
	byte[] getDocumentContent(
		@RequestParam(value = "id") final String id);

	@GetMapping(path = "/apifc/v1/Users/GetUsers", produces = APPLICATION_JSON_VALUE)
	List<User> getUsers(
		@RequestParam(value = "limit") final Integer limit,
		@RequestParam(value = "offset", required = false) final Integer offset,
		@RequestParam(value = "modifiedAfter", required = false) final String modifiedAfter,
		@RequestParam(value = "modifiedBefore", required = false) final String modifiedBefore);

	@GetMapping(path = "/apifc/v1/Actualisations/Proposals", produces = APPLICATION_JSON_VALUE)
	PersonBasedAktualiseringProposalDTO getActualisationProposal(
		@RequestParam(value = "personId") final String personId);

	@PostMapping(path = "/apifc/v1/Actualisations", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	Integer createActualisation(
		@RequestBody final PostAktualiseringsBodyRequest body);

	@GetMapping(path = "/apifc/v1/Calculations/Proposals", produces = APPLICATION_JSON_VALUE)
	PersonBasedCalculationProposalDTO getCalculationProposal(
		@RequestParam(value = "personId") final String personId);

	@PostMapping(path = "/apifc/v1/Calculations", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	Integer createCalculation(
		@RequestBody final PostCalculationBodyRequest body);

	@PostMapping(path = "/apifc/v1/Actualisations/{id}/Attachments", consumes = MULTIPART_FORM_DATA_VALUE)
	void postActualisationAttachment(
		@PathVariable final Integer id,
		@RequestPart("InsertDocumentType") final String documentType,
		@RequestPart("InsertDocumentSenderType") final String documentSenderType,
		@RequestPart(value = "Title", required = false) final String title,
		@RequestPart(value = "SenderName", required = false) final String senderName,
		@RequestPart("Content") final MultipartFile content);
}
