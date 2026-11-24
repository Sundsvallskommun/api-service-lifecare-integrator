package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebIntegration;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ProfessionalWebService {

	static final String BODY_NOT_A_JSON_OBJECT = "The body must be a JSON object";
	static final String DOCUMENT_ID_MISMATCH = "The body's documentId must be %s";
	static final String REMINDER_ID_MISMATCH = "The body's reminderId must be %s";

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final ProfessionalWebIntegration integration;
	private final PartyIntegration partyIntegration;

	public ProfessionalWebService(final ProfessionalWebIntegration integration, final PartyIntegration partyIntegration) {
		this.integration = integration;
		this.partyIntegration = partyIntegration;
	}

	public byte[] getCalculationProposal(final long serviceId) {
		return integration.getCalculationProposal(serviceId);
	}

	public byte[] getLatestCalculation(final long serviceId) {
		return integration.getLatestCalculation(serviceId);
	}

	public byte[] getCalculation(final long calculationId) {
		return integration.getCalculation(calculationId);
	}

	public byte[] getCalculationForEdit(final long calculationId) {
		return integration.getCalculationForEdit(calculationId);
	}

	public byte[] placePersons(final byte[] body) {
		requireJsonObject(body);
		return integration.placePersons(body);
	}

	public byte[] getCalculationAmount(final byte[] body) {
		requireJsonObject(body);
		return integration.getCalculationAmount(body);
	}

	public byte[] getCalculationSharedCost(final byte[] body) {
		requireJsonObject(body);
		return integration.getCalculationSharedCost(body);
	}

	public byte[] getCalculationJobStimuli(final byte[] body) {
		requireJsonObject(body);
		return integration.getCalculationJobStimuli(body);
	}

	public Optional<byte[]> createCalculation(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createCalculation(serviceId, body));
	}

	public Optional<byte[]> updateCalculation(final long calculationId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.updateCalculation(calculationId, body));
	}

	public void deleteCalculation(final long calculationId) {
		integration.deleteCalculation(calculationId);
	}

	public byte[] getJobStimulus(final long serviceId) {
		return integration.getJobStimulus(serviceId);
	}

	public byte[] getJobStimulusEndDate(final LocalDate fromDate) {
		return integration.getJobStimulusEndDate(fromDate);
	}

	public Optional<byte[]> saveJobStimulus(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.saveJobStimulus(body));
	}

	public byte[] getDecisionProposal(final long serviceId, final String amountType, final long calculationId) {
		return integration.getDecisionProposal(serviceId, amountType, calculationId);
	}

	public byte[] getDecisionReasons(final long decisionCode) {
		return integration.getDecisionReasons(decisionCode);
	}

	public byte[] getDecision(final long decisionId) {
		return integration.getDecision(decisionId);
	}

	public Optional<byte[]> createDecision(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createDecision(serviceId, body));
	}

	public Optional<byte[]> updateDecision(final long decisionId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.updateDecision(decisionId, body));
	}

	public byte[] getDocuments(final String municipalityId, final String partyId) {
		return integration.getDocuments(partyIntegration.getPersonNumber(municipalityId, partyId));
	}

	public byte[] getJournalNote(final long id) {
		return integration.getJournalNote(id);
	}

	public byte[] getJournalNoteForEdit(final long id) {
		return integration.getJournalNoteForEdit(id);
	}

	public byte[] getDocument(final long id) {
		return integration.getDocument(id);
	}

	public byte[] getDocumentForEdit(final long id) {
		return integration.getDocumentForEdit(id);
	}

	public byte[] getJournalNoteProposal(final long serviceId) {
		return integration.getJournalNoteProposal(serviceId);
	}

	public byte[] getDocumentProposal(final long serviceId) {
		return integration.getDocumentProposal(serviceId);
	}

	public Optional<byte[]> createJournalNote(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createJournalNote(body));
	}

	public Optional<byte[]> createDocument(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createDocument(body));
	}

	public Optional<byte[]> updateJournalNote(final long id, final byte[] body) {
		requireSameDocumentId(id, body);
		return toAnswer(integration.updateJournalNote(body));
	}

	public Optional<byte[]> updateDocument(final long id, final byte[] body) {
		requireSameDocumentId(id, body);
		return toAnswer(integration.updateDocument(body));
	}

	public byte[] getPaymentProposal(final long serviceId) {
		return integration.getPaymentProposal(serviceId);
	}

	public byte[] getLatestPayments(final long serviceId) {
		return integration.getLatestPayments(serviceId);
	}

	public byte[] hasHousehold(final long serviceId, final LocalDate date) {
		return integration.hasHousehold(serviceId, date);
	}

	public Optional<byte[]> createPayment(final long serviceId, final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createPayment(serviceId, body));
	}

	public Optional<byte[]> createPayee(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createPayee(body));
	}

	public byte[] getCalculationPdf(final long calculationId) {
		return integration.getCalculationPdf(calculationId);
	}

	public byte[] getDecisionPdf(final long decisionId) {
		return integration.getDecisionPdf(decisionId);
	}

	public byte[] getDocumentPdf(final long documentId) {
		return integration.getDocumentPdf(documentId);
	}

	public byte[] listReminders(final long serviceId) {
		return integration.listReminders(serviceId);
	}

	public byte[] getReminderProposal(final long serviceId) {
		return integration.getReminderProposal(serviceId);
	}

	public byte[] getReminder(final long reminderId) {
		return integration.getReminder(reminderId);
	}

	public Optional<byte[]> createReminder(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(integration.createReminder(body));
	}

	public void updateReminder(final long reminderId, final byte[] body) {
		Optional.ofNullable(requireJsonObject(body).get("reminderId"))
			.filter(JsonNode::canConvertToLong)
			.filter(bodyReminderId -> bodyReminderId.longValue() == reminderId)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, REMINDER_ID_MISMATCH.formatted(reminderId)));
		integration.updateReminder(body);
	}

	public void removeReminder(final long reminderId) {
		integration.removeReminder(reminderId);
	}

	private static void requireSameDocumentId(final long id, final byte[] body) {
		Optional.ofNullable(requireJsonObject(body).get("documentId"))
			.filter(JsonNode::isIntegralNumber)
			.filter(JsonNode::canConvertToLong)
			.filter(documentId -> documentId.longValue() == id)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, DOCUMENT_ID_MISMATCH.formatted(id)));
	}

	private static JsonNode requireJsonObject(final byte[] body) {
		return parse(body)
			.filter(JsonNode::isObject)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT));
	}

	private static Optional<byte[]> toAnswer(final byte[] answer) {
		return Optional.ofNullable(answer)
			.filter(bytes -> bytes.length > 0);
	}

	private static Optional<JsonNode> parse(final byte[] body) {
		try {
			return Optional.ofNullable(JSON.readTree(body));
		} catch (final JacksonException _) {
			return Optional.empty();
		}
	}
}
