package se.sundsvall.lifecareintegrator.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDocumentClient;
import tools.jackson.databind.JsonNode;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.requireJsonObject;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.toAnswer;

@Service
public class ProfessionalWebDocumentService {

	static final String DOCUMENT_ID_MISMATCH = "The body's documentId must be %s";

	private final ProfessionalWebDocumentClient client;
	private final PartyIntegration partyIntegration;

	public ProfessionalWebDocumentService(final ProfessionalWebDocumentClient client, final PartyIntegration partyIntegration) {
		this.client = client;
		this.partyIntegration = partyIntegration;
	}

	public byte[] getDocuments(final String municipalityId, final String partyId) {
		return client.getDocumentsListForClient(partyIntegration.getPersonNumber(municipalityId, partyId));
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

	public Optional<byte[]> createJournalNote(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.createJournalNote(body));
	}

	public Optional<byte[]> createDocument(final byte[] body) {
		requireJsonObject(body);
		return toAnswer(client.createDocument(body));
	}

	public Optional<byte[]> updateJournalNote(final long id, final byte[] body) {
		requireSameDocumentId(id, body);
		return toAnswer(client.updateJournalNote(body));
	}

	public Optional<byte[]> updateDocument(final long id, final byte[] body) {
		requireSameDocumentId(id, body);
		return toAnswer(client.updateDocument(body));
	}

	private static void requireSameDocumentId(final long id, final byte[] body) {
		Optional.ofNullable(requireJsonObject(body).get("documentId"))
			.filter(JsonNode::isIntegralNumber)
			.filter(JsonNode::canConvertToLong)
			.filter(documentId -> documentId.longValue() == id)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, DOCUMENT_ID_MISMATCH.formatted(id)));
	}
}
