package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebPrintProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebBusinessTypes.CALCULATION;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebBusinessTypes.DECISION;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebBusinessTypes.SERVICE;

@Component
public class ProfessionalWebIntegration {

	static final String NO_PERSON = "Lifecare named no person for the service";
	static final String NOT_A_PDF = "Lifecare answered the print with something that is not a PDF";
	static final String ASKED_AGAIN = "Lifecare asked the print's parameter questions again after they were answered";

	/** Lifecare's web app lists a service's calculations without an investigation. */
	private static final long NO_INVESTIGATION = 0;

	/** Lifecare's web app always asks for a new proposal, never for a stored one. */
	private static final int NEW_PROPOSAL = 0;

	/** The print container's owner for a calculation, as Lifecare's web app asks for it. */
	private static final String CALCULATION_OWNER_TYPE = "BERAK";
	private static final String CALCULATION_OWNER_CODE = "999999999";
	private static final byte[] PDF_SIGNATURE = "%PDF".getBytes(US_ASCII);

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final ProfessionalWebClient client;
	private final ProfessionalWebPrintProperties printProperties;

	public ProfessionalWebIntegration(final ProfessionalWebClient client, final ProfessionalWebPrintProperties printProperties) {
		this.client = client;
		this.printProperties = printProperties;
	}

	public byte[] getCalculationProposal(final long serviceId) {
		return client.getCalculationProposalForService(SERVICE, serviceId);
	}

	public byte[] getLatestCalculation(final long serviceId) {
		return client.listCalculations(SERVICE, serviceId, NO_INVESTIGATION, serviceId, true);
	}

	public byte[] getCalculation(final long calculationId) {
		return client.getCalculation(CALCULATION, calculationId);
	}

	public byte[] getCalculationForEdit(final long calculationId) {
		return client.getCalculationForEdit(CALCULATION, calculationId);
	}

	public byte[] placePersons(final byte[] body) {
		return client.placePersons(body);
	}

	public byte[] getCalculationAmount(final byte[] body) {
		return client.getCalculationAmount(body);
	}

	public byte[] getCalculationSharedCost(final byte[] body) {
		return client.getCalculationSharedCost(body);
	}

	public byte[] getCalculationJobStimuli(final byte[] body) {
		return client.getJobStimuliForCalculation(body);
	}

	public byte[] createCalculation(final long serviceId, final byte[] body) {
		return client.createCalculation(SERVICE, serviceId, body);
	}

	public byte[] updateCalculation(final long calculationId, final byte[] body) {
		return client.updateCalculation(CALCULATION, calculationId, body);
	}

	public void deleteCalculation(final long calculationId) {
		client.deleteCalculation(CALCULATION, calculationId, new byte[0]);
	}

	public byte[] getJobStimulus(final long serviceId) {
		return client.getJobStimulusForService(SERVICE, serviceId);
	}

	public byte[] getJobStimulusEndDate(final LocalDate fromDate) {
		return client.getJobStimulusToDate(fromDate.toString());
	}

	public byte[] saveJobStimulus(final byte[] body) {
		return client.saveJobStimulus(body);
	}

	public byte[] getDecisionProposal(final long serviceId, final String amountType, final long calculationId) {
		return client.getDecisionProposalForService(SERVICE, serviceId, amountType, calculationId, NEW_PROPOSAL);
	}

	public byte[] getDecisionReasons(final long decisionCode) {
		return client.getMappedDecisionReasons(decisionCode);
	}

	public byte[] getDecision(final long decisionId) {
		return client.getDecision(DECISION, decisionId);
	}

	public byte[] createDecision(final long serviceId, final byte[] body) {
		return client.createDecision(SERVICE, serviceId, body);
	}

	public byte[] updateDecision(final long decisionId, final byte[] body) {
		return client.updateDecision(DECISION, decisionId, body);
	}

	public byte[] getDocuments(final String personNumber) {
		return client.getDocumentsListForClient(personNumber);
	}

	public byte[] getJournalNote(final long id) {
		return client.getJournalNoteWithContent(id, true, false);
	}

	public byte[] getJournalNoteForEdit(final long id) {
		return client.getJournalNoteWithContent(id, false, true);
	}

	public byte[] getDocument(final long id) {
		return client.getDocumentWithContent(id, true, false);
	}

	public byte[] getDocumentForEdit(final long id) {
		return client.getDocumentWithContent(id, false, true);
	}

	public byte[] getJournalNoteProposal(final long serviceId) {
		return client.getNoteProposalForService(serviceId);
	}

	public byte[] getDocumentProposal(final long serviceId) {
		return client.getDocumentProposalForService(serviceId);
	}

	public byte[] createJournalNote(final byte[] body) {
		return client.createJournalNote(body);
	}

	public byte[] createDocument(final byte[] body) {
		return client.createDocument(body);
	}

	public byte[] updateJournalNote(final byte[] body) {
		return client.updateJournalNote(body);
	}

	public byte[] updateDocument(final byte[] body) {
		return client.updateDocument(body);
	}

	public byte[] getPaymentProposal(final long serviceId) {
		return client.getPaymentForCreate(SERVICE, serviceId);
	}

	public byte[] getLatestPayments(final long serviceId) {
		return client.getLatestPayments(SERVICE, serviceId);
	}

	public byte[] hasHousehold(final long serviceId, final LocalDate date) {
		final var personId = findPersonId(client.getPaymentForCreate(SERVICE, serviceId))
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, NO_PERSON));
		return client.hasHouseholdThisDate(personId, date.toString());
	}

	public byte[] createPayment(final long serviceId, final byte[] body) {
		return client.createPayment(SERVICE, serviceId, body);
	}

	public byte[] createPayee(final byte[] body) {
		return client.createPayee(body);
	}

	public byte[] getCalculationPdf(final long calculationId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", printProperties.calculationTemplateId());
		query.put("ownerType", CALCULATION_OWNER_TYPE);
		query.put("ownerCode", CALCULATION_OWNER_CODE);
		query.put("objectId", String.valueOf(calculationId));
		return print("PrintContainer", query);
	}

	public byte[] getDecisionPdf(final long decisionId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", printProperties.decisionTemplateId());
		query.put("decisionId", String.valueOf(decisionId));
		query.put("hideRevisions", "true");
		return print("PrintDecision", query);
	}

	public byte[] getDocumentPdf(final long documentId) {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", printProperties.documentTemplateId());
		query.put("documentId", String.valueOf(documentId));
		query.put("hideRevisions", "true");
		return print("PrintDocument", query);
	}

	public byte[] listReminders(final long serviceId) {
		return client.listRemindersByServiceId(serviceId);
	}

	public byte[] getReminderProposal(final long serviceId) {
		return client.getReminderProposalForService(serviceId);
	}

	public byte[] getReminder(final long reminderId) {
		return client.getReminderEditComposite(reminderId, false);
	}

	public byte[] createReminder(final byte[] body) {
		return client.createReminder(body);
	}

	public void updateReminder(final byte[] body) {
		client.updateReminder(body);
	}

	public void removeReminder(final long reminderId) {
		client.removeReminder(("{\"id\":" + reminderId + "}").getBytes(UTF_8));
	}

	// The person Lifecare's own payment form asks about is the one its underlag names in payment.susPersonId
	private static Optional<String> findPersonId(final byte[] paymentForCreate) {
		return parse(paymentForCreate)
			.map(underlag -> underlag.path("payment").path("susPersonId"))
			.filter(JsonNode::isValueNode)
			.filter(personId -> !personId.isNull())
			.map(JsonNode::asString)
			.filter(StringUtils::hasText);
	}

	private static Optional<JsonNode> parse(final byte[] body) {
		try {
			return Optional.ofNullable(JSON.readTree(body));
		} catch (final JacksonException _) {
			return Optional.empty();
		}
	}

	private byte[] print(final String print, final Map<String, String> query) {
		final var answer = client.renderPrint(print, query);
		if (isPdf(answer)) {
			return answer;
		}
		final var form = ProfessionalWebParameterForm.answerWithDefaults(answer)
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, NOT_A_PDF));
		final var rendered = client.submitPrintParameters(print, query, form);
		if (isPdf(rendered)) {
			return rendered;
		}
		if (ProfessionalWebParameterForm.isParameterQuestions(rendered)) {
			throw Problem.valueOf(BAD_GATEWAY, ASKED_AGAIN);
		}
		throw Problem.valueOf(BAD_GATEWAY, NOT_A_PDF);
	}

	private static boolean isPdf(final byte[] answer) {
		return answer != null && answer.length >= PDF_SIGNATURE.length
			&& Arrays.equals(answer, 0, PDF_SIGNATURE.length, PDF_SIGNATURE, 0, PDF_SIGNATURE.length);
	}
}
