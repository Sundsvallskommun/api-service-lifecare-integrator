package se.sundsvall.lifecareintegrator.integration.professionalweb;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebApi2Configuration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.lifecare-professionalweb.url:}", configuration = ProfessionalWebApi2Configuration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ProfessionalWebClient {

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimulusToDate", produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimulusToDate(
		@RequestParam(value = "id") final String fromDate);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/SaveJobStimulus", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] saveJobStimulus(
		@RequestBody final byte[] body);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetProposalService", produces = APPLICATION_JSON_VALUE)
	byte[] getCalculationProposalForService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/ListCalculations", produces = APPLICATION_JSON_VALUE)
	byte[] listCalculations(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestParam(value = "investigationId") final long investigationId,
		@RequestParam(value = "serviceId") final long serviceId,
		@RequestParam(value = "onlylatest") final boolean onlyLatest);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetCalculation", produces = APPLICATION_JSON_VALUE)
	byte[] getCalculation(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetCalculationForEdit", produces = APPLICATION_JSON_VALUE)
	byte[] getCalculationForEdit(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimulusForService", produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimulusForService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/PlacePersons", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] placePersons(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetAmount", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getCalculationAmount(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetSharedCost", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getCalculationSharedCost(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/GetJobStimuliForCalculation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] getJobStimuliForCalculation(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createCalculation(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateCalculation(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@DeleteMapping(path = "/WESE.FC.ProfessionalWeb/api2/Calculation/Delete", consumes = APPLICATION_JSON_VALUE)
	void deleteCalculation(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getDecisionProposalForService(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestParam(value = "amountType") final String amountType,
		@RequestParam(value = "calculationId") final long calculationId,
		@RequestParam(value = "proposalId") final int proposalId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetMappedDecisionReasons", produces = APPLICATION_JSON_VALUE)
	byte[] getMappedDecisionReasons(
		@RequestParam(value = "id") final long decisionCode);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/GetDecision", produces = APPLICATION_JSON_VALUE)
	byte[] getDecision(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createDecision(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Decision/Update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateDecision(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	// Document paths as Lifecare's web app sends them: the proposals have no trailing slash, every other Document path
	// needs one
	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentsListForClient/", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentsListForClient(
		@RequestParam(value = "id") final String personNumber);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetJournalNoteWithContent/", produces = APPLICATION_JSON_VALUE)
	byte[] getJournalNoteWithContent(
		@RequestParam(value = "id") final long id,
		@RequestParam(value = "hideRevisions") final boolean hideRevisions,
		@RequestParam(value = "inEdit") final boolean inEdit);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentWithContent/", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentWithContent(
		@RequestParam(value = "id") final long id,
		@RequestParam(value = "hideRevisions") final boolean hideRevisions,
		@RequestParam(value = "inEdit") final boolean inEdit);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetNoteProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getNoteProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/GetDocumentProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getDocumentProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/CreateJournalNote/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createJournalNote(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/CreateDocument/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createDocument(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/UpdateJournalNote/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateJournalNote(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Document/UpdateDocument/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] updateDocument(
		@RequestBody final byte[] body);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/GetPaymentForCreate", produces = APPLICATION_JSON_VALUE)
	byte[] getPaymentForCreate(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/GetLatestPayments", produces = APPLICATION_JSON_VALUE)
	byte[] getLatestPayments(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId);

	// The trailing slash is needed, as Lifecare's web app sends it
	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Household/HasHouseholdThisDate/", produces = APPLICATION_JSON_VALUE)
	byte[] hasHouseholdThisDate(
		@RequestParam(value = "personId") final String personId,
		@RequestParam(value = "date") final String date);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payment/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createPayment(
		@RequestParam(value = "businessType") final int businessType,
		@RequestParam(value = "businessId") final long businessId,
		@RequestBody final byte[] body);

	// The missing trailing slash is needed, as Lifecare's web app sends it
	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Payee/Create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createPayee(
		@RequestBody final byte[] body);

	// Lifecare's own prints, outside api2: the pages its web app opens to print, answered with a PDF
	@GetMapping(path = "/WESE.FC.ProfessionalWeb/RenderPdf/{print}")
	byte[] renderPrint(
		@PathVariable("print") final String print,
		@RequestParam final Map<String, String> query);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/RenderPdf/{print}", consumes = APPLICATION_FORM_URLENCODED_VALUE)
	byte[] submitPrintParameters(
		@PathVariable("print") final String print,
		@RequestParam final Map<String, String> query,
		@RequestBody final byte[] form);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/ListRemindersByServiceId", produces = APPLICATION_JSON_VALUE)
	byte[] listRemindersByServiceId(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/GetProposalForService", produces = APPLICATION_JSON_VALUE)
	byte[] getReminderProposalForService(
		@RequestParam(value = "id") final long serviceId);

	@GetMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/GetReminderEditComposite", produces = APPLICATION_JSON_VALUE)
	byte[] getReminderEditComposite(
		@RequestParam(value = "reminderId") final long reminderId,
		@RequestParam(value = "readOptions") final boolean readOptions);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	byte[] createReminder(
		@RequestBody final byte[] body);

	@PostMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/UpdateReminder/", consumes = APPLICATION_JSON_VALUE)
	void updateReminder(
		@RequestBody final byte[] body);

	@DeleteMapping(path = "/WESE.FC.ProfessionalWeb/api2/Reminders/RemoveReminder/", consumes = APPLICATION_JSON_VALUE)
	void removeReminder(
		@RequestBody final byte[] body);
}
