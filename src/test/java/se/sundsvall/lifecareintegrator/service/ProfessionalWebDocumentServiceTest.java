package se.sundsvall.lifecareintegrator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.party.PartyIntegration;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDocumentClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebBodies.BODY_NOT_A_JSON_OBJECT;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebDocumentServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final byte[] LIFECARE_JSON = "{\"documentId\":1,\"content\":\"Anteckning åäö\",\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] UPDATE_BODY = "{\"documentId\": 1, \"content\": \"Anteckning åäö\", \"unknown\": {\"a\": [1]}}".getBytes(UTF_8);
	private static final byte[] CREATE_BODY = "{ \"serviceId\": 42,\n \"content\": \"Anteckning åäö\" }".getBytes(UTF_8);

	@Mock
	private ProfessionalWebDocumentClient clientMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private ProfessionalWebDocumentService service;

	@Test
	void getDocumentsAsksWithThePersonsPersonnummer() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn("191212121212");
		when(clientMock.getDocumentsListForClient("191212121212")).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(service.getDocuments(MUNICIPALITY_ID, PARTY_ID), UTF_8)).isEqualTo("[]");

		verify(clientMock).getDocumentsListForClient("191212121212");
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentsForAPartyIdPartyDoesNotKnowAsksNothingOfLifecare() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'"));

		assertThatThrownBy(() -> service.getDocuments(MUNICIPALITY_ID, PARTY_ID))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus)
			.isEqualTo(NOT_FOUND);

		verifyNoInteractions(clientMock);
	}

	@Test
	void getJournalNoteReadsForDisplay() {
		when(clientMock.getJournalNoteWithContent(1, true, false)).thenReturn(LIFECARE_JSON);

		assertThat(service.getJournalNote(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJournalNoteWithContent(1, true, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJournalNoteForEditReadsWithRevisionsInEdit() {
		when(clientMock.getJournalNoteWithContent(1, false, true)).thenReturn(LIFECARE_JSON);

		assertThat(service.getJournalNoteForEdit(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJournalNoteWithContent(1, false, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentReadsForDisplay() {
		when(clientMock.getDocumentWithContent(1, true, false)).thenReturn(LIFECARE_JSON);

		assertThat(service.getDocument(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDocumentWithContent(1, true, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentForEditReadsWithRevisionsInEdit() {
		when(clientMock.getDocumentWithContent(1, false, true)).thenReturn(LIFECARE_JSON);

		assertThat(service.getDocumentForEdit(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDocumentWithContent(1, false, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void proposalsAskForTheService() {
		when(clientMock.getNoteProposalForService(42)).thenReturn("{\"note\":1}".getBytes(UTF_8));
		when(clientMock.getDocumentProposalForService(42)).thenReturn("{\"document\":1}".getBytes(UTF_8));

		assertThat(new String(service.getJournalNoteProposal(42), UTF_8)).isEqualTo("{\"note\":1}");
		assertThat(new String(service.getDocumentProposal(42), UTF_8)).isEqualTo("{\"document\":1}");

		verify(clientMock).getNoteProposalForService(42);
		verify(clientMock).getDocumentProposalForService(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createJournalNoteSendsTheBodyUnchanged() {
		when(clientMock.createJournalNote(CREATE_BODY)).thenReturn("{\"documentId\":1}".getBytes(UTF_8));

		assertThat(service.createJournalNote(CREATE_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"documentId\":1}"));

		verify(clientMock).createJournalNote(CREATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createDocumentSendsTheBodyUnchanged() {
		when(clientMock.createDocument(CREATE_BODY)).thenReturn(new byte[0]);

		assertThat(service.createDocument(CREATE_BODY)).isEmpty();

		verify(clientMock).createDocument(CREATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateJournalNoteSendsTheBodyUnchanged() {
		when(clientMock.updateJournalNote(UPDATE_BODY)).thenReturn(LIFECARE_JSON);

		assertThat(service.updateJournalNote(1, UPDATE_BODY)).hasValue(LIFECARE_JSON);

		verify(clientMock).updateJournalNote(UPDATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateDocumentSendsTheBodyUnchanged() {
		when(clientMock.updateDocument(UPDATE_BODY)).thenReturn(null);

		assertThat(service.updateDocument(1, UPDATE_BODY)).isEmpty();

		verify(clientMock).updateDocument(UPDATE_BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"documentId\": 2}", "{\"content\": \"x\"}", "{\"documentId\": \"1\"}", "{\"documentId\": null}", "{\"documentId\": 1.5}", "{\"documentId\": 1.0}",
		"{\"documentId\": 99999999999999999999}", "{\"documentId\": true}", "{\"nested\": {\"documentId\": 1}}"
	})
	void updateWhoseBodyDoesNotNameTheSameDocumentAsksNothingOfLifecare(final String body) {
		assertThatThrownBy(() -> service.updateJournalNote(1, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, "The body's documentId must be 1");
		assertThatThrownBy(() -> service.updateDocument(1, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, "The body's documentId must be 1");

		verifyNoInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"documentId\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void writeWithABodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		final var bytes = body.getBytes(UTF_8);
		assertThatThrownBy(() -> service.createJournalNote(bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.createDocument(bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateJournalNote(1, bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateDocument(1, bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(clientMock);
	}
}
