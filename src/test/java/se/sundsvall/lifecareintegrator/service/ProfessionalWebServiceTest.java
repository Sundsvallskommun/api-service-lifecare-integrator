package se.sundsvall.lifecareintegrator.service;

import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
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
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebIntegration;

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
import static se.sundsvall.lifecareintegrator.service.ProfessionalWebService.BODY_NOT_A_JSON_OBJECT;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebServiceTest {

	private static final byte[] CALCULATION_JSON = "{\"calculationId\":7,\"personnummer\":\"191212121212\",\"rows\":[{\"amount\":12.50}]}".getBytes(UTF_8);
	private static final byte[] CALCULATION_BODY = "{ \"calculationId\": 7,\n \"text\": \"Beräkning åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final byte[] DECISION_JSON = "{\"decisionId\":9,\"personnummer\":\"191212121212\",\"unknown\":[1]}".getBytes(UTF_8);
	private static final byte[] DECISION_BODY = "{ \"decisionId\": 9,\n \"text\": \"Beslut åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final byte[] DOCUMENT_JSON = "{\"documentId\":1,\"content\":\"Anteckning åäö\",\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] DOCUMENT_UPDATE_BODY = "{\"documentId\": 1, \"content\": \"Anteckning åäö\", \"unknown\": {\"a\": [1]}}".getBytes(UTF_8);
	private static final byte[] DOCUMENT_CREATE_BODY = "{ \"serviceId\": 42,\n \"content\": \"Anteckning åäö\" }".getBytes(UTF_8);
	private static final byte[] JOB_STIMULUS_BODY = "{ \"personId\": \"19121212-1212\",\n \"text\": \"åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final byte[] PAYMENT_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] PAYMENT_BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }".getBytes(UTF_8);
	private static final byte[] PDF = "%PDF-1.4\nbinaryå".getBytes(UTF_8);
	private static final byte[] REMINDER_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}".getBytes(UTF_8);
	private static final byte[] REMINDER_CREATE_BODY = "{ \"serviceId\": 42,\n  \"text\": \"Påminnelse åäö\", \"personnummer\": \"19121212-1212\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final byte[] REMINDER_UPDATE_BODY = "{\"reminderId\": 7, \"text\": \"Påminnelse åäö\", \"personnummer\": \"191212121212\", \"unknown\": {\"a\": [1]}}".getBytes(UTF_8);

	@Mock
	private ProfessionalWebIntegration integrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private ProfessionalWebService service;

	@AfterEach
	void verifyNoUnexpectedPartyLookups() {
		verifyNoMoreInteractions(partyIntegrationMock);
	}

	@Test
	void getCalculationProposal() {
		when(integrationMock.getCalculationProposal(42)).thenReturn(CALCULATION_JSON);

		assertThat(service.getCalculationProposal(42)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).getCalculationProposal(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getLatestCalculation() {
		when(integrationMock.getLatestCalculation(42)).thenReturn(CALCULATION_JSON);

		assertThat(service.getLatestCalculation(42)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).getLatestCalculation(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getCalculation() {
		when(integrationMock.getCalculation(7)).thenReturn(CALCULATION_JSON);

		assertThat(service.getCalculation(7)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).getCalculation(7);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getCalculationForEdit() {
		when(integrationMock.getCalculationForEdit(7)).thenReturn(CALCULATION_JSON);

		assertThat(service.getCalculationForEdit(7)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).getCalculationForEdit(7);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void placePersonsSendsTheBodyUnchanged() {
		when(integrationMock.placePersons(CALCULATION_BODY)).thenReturn(CALCULATION_JSON);

		assertThat(service.placePersons(CALCULATION_BODY)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).placePersons(CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getCalculationAmountSendsTheBodyUnchanged() {
		when(integrationMock.getCalculationAmount(CALCULATION_BODY)).thenReturn("{\"amount\":1250.50}".getBytes(UTF_8));

		assertThat(new String(service.getCalculationAmount(CALCULATION_BODY), UTF_8)).isEqualTo("{\"amount\":1250.50}");

		verify(integrationMock).getCalculationAmount(CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getCalculationSharedCostSendsTheBodyUnchanged() {
		when(integrationMock.getCalculationSharedCost(CALCULATION_BODY)).thenReturn("1250.50".getBytes(UTF_8));

		assertThat(new String(service.getCalculationSharedCost(CALCULATION_BODY), UTF_8)).isEqualTo("1250.50");

		verify(integrationMock).getCalculationSharedCost(CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getCalculationJobStimuliSendsTheBodyUnchanged() {
		when(integrationMock.getCalculationJobStimuli(CALCULATION_BODY)).thenReturn(CALCULATION_JSON);

		assertThat(service.getCalculationJobStimuli(CALCULATION_BODY)).isEqualTo(CALCULATION_JSON);

		verify(integrationMock).getCalculationJobStimuli(CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createCalculationSendsTheBodyUnchanged() {
		when(integrationMock.createCalculation(42, CALCULATION_BODY)).thenReturn(CALCULATION_JSON);

		assertThat(service.createCalculation(42, CALCULATION_BODY)).hasValue(CALCULATION_JSON);

		verify(integrationMock).createCalculation(42, CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createCalculationWithAnEmptyAnswerReturnsNothing() {
		when(integrationMock.createCalculation(42, CALCULATION_BODY)).thenReturn(new byte[0]);

		assertThat(service.createCalculation(42, CALCULATION_BODY)).isEmpty();
	}

	@Test
	void updateCalculationSendsTheBodyUnchanged() {
		when(integrationMock.updateCalculation(7, CALCULATION_BODY)).thenReturn(CALCULATION_JSON);

		assertThat(service.updateCalculation(7, CALCULATION_BODY)).hasValue(CALCULATION_JSON);

		verify(integrationMock).updateCalculation(7, CALCULATION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void updateCalculationWithANullAnswerReturnsNothing() {
		when(integrationMock.updateCalculation(7, CALCULATION_BODY)).thenReturn(null);

		assertThat(service.updateCalculation(7, CALCULATION_BODY)).isEmpty();
	}

	@Test
	void deleteCalculation() {
		service.deleteCalculation(7);

		verify(integrationMock).deleteCalculation(7);
		verifyNoMoreInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"a\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void calculationBodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		final var bytes = body.getBytes(UTF_8);
		assertBadBody(() -> service.placePersons(bytes));
		assertBadBody(() -> service.getCalculationAmount(bytes));
		assertBadBody(() -> service.getCalculationSharedCost(bytes));
		assertBadBody(() -> service.getCalculationJobStimuli(bytes));
		assertBadBody(() -> service.createCalculation(42, bytes));
		assertBadBody(() -> service.updateCalculation(7, bytes));

		verifyNoInteractions(integrationMock);
	}

	@Test
	void getDecisionProposal() {
		when(integrationMock.getDecisionProposal(42, "TotalSum", 7)).thenReturn(DECISION_JSON);

		assertThat(service.getDecisionProposal(42, "TotalSum", 7)).isEqualTo(DECISION_JSON);

		verify(integrationMock).getDecisionProposal(42, "TotalSum", 7);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDecisionProposalPassesAnEmptyAmountTypeAndCalculationIdZeroAsTheyAre() {
		when(integrationMock.getDecisionProposal(42, "", 0)).thenReturn(DECISION_JSON);

		assertThat(service.getDecisionProposal(42, "", 0)).isEqualTo(DECISION_JSON);

		verify(integrationMock).getDecisionProposal(42, "", 0);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDecisionReasonsAsksWithTheDecisionCode() {
		when(integrationMock.getDecisionReasons(5)).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(service.getDecisionReasons(5), UTF_8)).isEqualTo("[]");

		verify(integrationMock).getDecisionReasons(5);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDecision() {
		when(integrationMock.getDecision(9)).thenReturn(DECISION_JSON);

		assertThat(service.getDecision(9)).isEqualTo(DECISION_JSON);

		verify(integrationMock).getDecision(9);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createDecisionSendsTheBodyUnchanged() {
		when(integrationMock.createDecision(42, DECISION_BODY)).thenReturn(DECISION_JSON);

		assertThat(service.createDecision(42, DECISION_BODY)).hasValue(DECISION_JSON);

		verify(integrationMock).createDecision(42, DECISION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createDecisionWithAnEmptyAnswerReturnsNothing() {
		when(integrationMock.createDecision(42, DECISION_BODY)).thenReturn(new byte[0]);

		assertThat(service.createDecision(42, DECISION_BODY)).isEmpty();
	}

	@Test
	void updateDecisionSendsTheBodyUnchanged() {
		when(integrationMock.updateDecision(9, DECISION_BODY)).thenReturn(DECISION_JSON);

		assertThat(service.updateDecision(9, DECISION_BODY)).hasValue(DECISION_JSON);

		verify(integrationMock).updateDecision(9, DECISION_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void updateDecisionWithANullAnswerReturnsNothing() {
		when(integrationMock.updateDecision(9, DECISION_BODY)).thenReturn(null);

		assertThat(service.updateDecision(9, DECISION_BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"decisionId\": 9}]", "\"text\"", "42", "null", "{not json", ""
	})
	void decisionWriteWithABodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		assertThatThrownBy(() -> service.createDecision(42, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateDecision(9, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void getDocumentsAsksWithThePersonsPersonnummer() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenReturn("191212121212");
		when(integrationMock.getDocuments("191212121212")).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(service.getDocuments(MUNICIPALITY_ID, PARTY_ID), UTF_8)).isEqualTo("[]");

		verify(partyIntegrationMock).getPersonNumber(MUNICIPALITY_ID, PARTY_ID);
		verify(integrationMock).getDocuments("191212121212");
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDocumentsForAPartyIdPartyDoesNotKnowAsksNothingOfLifecare() {
		when(partyIntegrationMock.getPersonNumber(MUNICIPALITY_ID, PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'"));

		assertThatThrownBy(() -> service.getDocuments(MUNICIPALITY_ID, PARTY_ID))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'");

		verify(partyIntegrationMock).getPersonNumber(MUNICIPALITY_ID, PARTY_ID);
		verifyNoInteractions(integrationMock);
	}

	@Test
	void getJournalNote() {
		when(integrationMock.getJournalNote(1)).thenReturn(DOCUMENT_JSON);

		assertThat(service.getJournalNote(1)).isEqualTo(DOCUMENT_JSON);

		verify(integrationMock).getJournalNote(1);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getJournalNoteForEdit() {
		when(integrationMock.getJournalNoteForEdit(1)).thenReturn(DOCUMENT_JSON);

		assertThat(service.getJournalNoteForEdit(1)).isEqualTo(DOCUMENT_JSON);

		verify(integrationMock).getJournalNoteForEdit(1);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDocument() {
		when(integrationMock.getDocument(1)).thenReturn(DOCUMENT_JSON);

		assertThat(service.getDocument(1)).isEqualTo(DOCUMENT_JSON);

		verify(integrationMock).getDocument(1);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDocumentForEdit() {
		when(integrationMock.getDocumentForEdit(1)).thenReturn(DOCUMENT_JSON);

		assertThat(service.getDocumentForEdit(1)).isEqualTo(DOCUMENT_JSON);

		verify(integrationMock).getDocumentForEdit(1);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void proposalsAskForTheService() {
		when(integrationMock.getJournalNoteProposal(42)).thenReturn("{\"note\":1}".getBytes(UTF_8));
		when(integrationMock.getDocumentProposal(42)).thenReturn("{\"document\":1}".getBytes(UTF_8));

		assertThat(new String(service.getJournalNoteProposal(42), UTF_8)).isEqualTo("{\"note\":1}");
		assertThat(new String(service.getDocumentProposal(42), UTF_8)).isEqualTo("{\"document\":1}");

		verify(integrationMock).getJournalNoteProposal(42);
		verify(integrationMock).getDocumentProposal(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createJournalNoteSendsTheBodyUnchanged() {
		when(integrationMock.createJournalNote(DOCUMENT_CREATE_BODY)).thenReturn("{\"documentId\":1}".getBytes(UTF_8));

		assertThat(service.createJournalNote(DOCUMENT_CREATE_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"documentId\":1}"));

		verify(integrationMock).createJournalNote(DOCUMENT_CREATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createDocumentSendsTheBodyUnchanged() {
		when(integrationMock.createDocument(DOCUMENT_CREATE_BODY)).thenReturn(new byte[0]);

		assertThat(service.createDocument(DOCUMENT_CREATE_BODY)).isEmpty();

		verify(integrationMock).createDocument(DOCUMENT_CREATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void updateJournalNoteSendsTheBodyUnchanged() {
		when(integrationMock.updateJournalNote(DOCUMENT_UPDATE_BODY)).thenReturn(DOCUMENT_JSON);

		assertThat(service.updateJournalNote(1, DOCUMENT_UPDATE_BODY)).hasValue(DOCUMENT_JSON);

		verify(integrationMock).updateJournalNote(DOCUMENT_UPDATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void updateDocumentSendsTheBodyUnchanged() {
		when(integrationMock.updateDocument(DOCUMENT_UPDATE_BODY)).thenReturn(null);

		assertThat(service.updateDocument(1, DOCUMENT_UPDATE_BODY)).isEmpty();

		verify(integrationMock).updateDocument(DOCUMENT_UPDATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"documentId\": 2}", "{\"content\": \"x\"}", "{\"documentId\": \"1\"}", "{\"documentId\": null}", "{\"documentId\": 1.5}", "{\"documentId\": 1.0}",
		"{\"documentId\": 99999999999999999999}", "{\"documentId\": true}", "{\"nested\": {\"documentId\": 1}}",
		"{\"documentId\": 1, \"DocumentId\": 2}"
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

		verifyNoInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"documentId\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void documentWriteWithABodyThatIsNotAJsonObjectAsksNothingOfLifecare(final String body) {
		final var bytes = body.getBytes(UTF_8);
		assertThatThrownBy(() -> service.createJournalNote(bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.createDocument(bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateJournalNote(1, bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
		assertThatThrownBy(() -> service.updateDocument(1, bytes)).asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail).containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void getJobStimulus() {
		when(integrationMock.getJobStimulus(42)).thenReturn("{\"periods\":[]}".getBytes(UTF_8));

		assertThat(new String(service.getJobStimulus(42), UTF_8)).isEqualTo("{\"periods\":[]}");

		verify(integrationMock).getJobStimulus(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getJobStimulusEndDate() {
		when(integrationMock.getJobStimulusEndDate(LocalDate.of(2026, 1, 5))).thenReturn("\"2028-01-04\"".getBytes(UTF_8));

		assertThat(new String(service.getJobStimulusEndDate(LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("\"2028-01-04\"");

		verify(integrationMock).getJobStimulusEndDate(LocalDate.of(2026, 1, 5));
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void saveJobStimulusSendsTheBodyUnchangedAndReturnsLifecaresAnswer() {
		when(integrationMock.saveJobStimulus(JOB_STIMULUS_BODY)).thenReturn("{\"saved\":true}".getBytes(UTF_8));

		assertThat(service.saveJobStimulus(JOB_STIMULUS_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"saved\":true}"));

		verify(integrationMock).saveJobStimulus(JOB_STIMULUS_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void saveJobStimulusWithAnEmptyAnswerReturnsNothing() {
		when(integrationMock.saveJobStimulus(JOB_STIMULUS_BODY)).thenReturn(new byte[0]);

		assertThat(service.saveJobStimulus(JOB_STIMULUS_BODY)).isEmpty();
	}

	@Test
	void saveJobStimulusWithANullAnswerReturnsNothing() {
		when(integrationMock.saveJobStimulus(JOB_STIMULUS_BODY)).thenReturn(null);

		assertThat(service.saveJobStimulus(JOB_STIMULUS_BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"a\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void saveJobStimulusWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.saveJobStimulus(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void getPaymentProposal() {
		when(integrationMock.getPaymentProposal(42)).thenReturn(PAYMENT_JSON);

		assertThat(service.getPaymentProposal(42)).isEqualTo(PAYMENT_JSON);

		verify(integrationMock).getPaymentProposal(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getLatestPayments() {
		when(integrationMock.getLatestPayments(42)).thenReturn(PAYMENT_JSON);

		assertThat(service.getLatestPayments(42)).isEqualTo(PAYMENT_JSON);

		verify(integrationMock).getLatestPayments(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void hasHousehold() {
		when(integrationMock.hasHousehold(42, LocalDate.of(2026, 1, 5))).thenReturn("false".getBytes(UTF_8));

		assertThat(new String(service.hasHousehold(42, LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("false");

		verify(integrationMock).hasHousehold(42, LocalDate.of(2026, 1, 5));
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createPaymentSendsTheBodyUnchanged() {
		when(integrationMock.createPayment(42, PAYMENT_BODY)).thenReturn("{\"paymentId\":5}".getBytes(UTF_8));

		assertThat(service.createPayment(42, PAYMENT_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"paymentId\":5}"));

		verify(integrationMock).createPayment(42, PAYMENT_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createPaymentWithAnEmptyAnswerReturnsNothing() {
		when(integrationMock.createPayment(42, PAYMENT_BODY)).thenReturn(new byte[0]);

		assertThat(service.createPayment(42, PAYMENT_BODY)).isEmpty();
	}

	@Test
	void createPayeeSendsTheBodyUnchanged() {
		when(integrationMock.createPayee(PAYMENT_BODY)).thenReturn("{\"payeeId\":3}".getBytes(UTF_8));

		assertThat(service.createPayee(PAYMENT_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"payeeId\":3}"));

		verify(integrationMock).createPayee(PAYMENT_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createPayeeWithANullAnswerReturnsNothing() {
		when(integrationMock.createPayee(PAYMENT_BODY)).thenReturn(null);

		assertThat(service.createPayee(PAYMENT_BODY)).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"amount\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void createPaymentWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createPayment(42, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"amount\": 1}]", "\"text\"", "42", "null", "{not json", ""
	})
	void createPayeeWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createPayee(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void getCalculationPdf() {
		when(integrationMock.getCalculationPdf(7)).thenReturn(PDF);

		assertThat(service.getCalculationPdf(7)).isEqualTo(PDF);

		verify(integrationMock).getCalculationPdf(7);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDecisionPdf() {
		when(integrationMock.getDecisionPdf(9)).thenReturn(PDF);

		assertThat(service.getDecisionPdf(9)).isEqualTo(PDF);

		verify(integrationMock).getDecisionPdf(9);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getDocumentPdf() {
		when(integrationMock.getDocumentPdf(1)).thenReturn(PDF);

		assertThat(service.getDocumentPdf(1)).isEqualTo(PDF);

		verify(integrationMock).getDocumentPdf(1);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void listReminders() {
		when(integrationMock.listReminders(42)).thenReturn(REMINDER_JSON);

		assertThat(service.listReminders(42)).isEqualTo(REMINDER_JSON);

		verify(integrationMock).listReminders(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getReminderProposal() {
		when(integrationMock.getReminderProposal(42)).thenReturn(REMINDER_JSON);

		assertThat(service.getReminderProposal(42)).isEqualTo(REMINDER_JSON);

		verify(integrationMock).getReminderProposal(42);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void getReminder() {
		when(integrationMock.getReminder(7)).thenReturn(REMINDER_JSON);

		assertThat(service.getReminder(7)).isEqualTo(REMINDER_JSON);

		verify(integrationMock).getReminder(7);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createReminderSendsTheBodyUnchangedAndReturnsLifecaresAnswer() {
		when(integrationMock.createReminder(REMINDER_CREATE_BODY)).thenReturn("{\"id\":9}".getBytes(UTF_8));

		assertThat(service.createReminder(REMINDER_CREATE_BODY)).hasValueSatisfying(answer -> assertThat(new String(answer, UTF_8)).isEqualTo("{\"id\":9}"));

		verify(integrationMock).createReminder(REMINDER_CREATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createReminderWithAnEmptyLifecareAnswerReturnsNothing() {
		when(integrationMock.createReminder("{}".getBytes(UTF_8))).thenReturn(new byte[0]);

		assertThat(service.createReminder("{}".getBytes(UTF_8))).isEmpty();

		verify(integrationMock).createReminder("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(integrationMock);
	}

	@Test
	void createReminderWithANullLifecareAnswerReturnsNothing() {
		when(integrationMock.createReminder("{}".getBytes(UTF_8))).thenReturn(null);

		assertThat(service.createReminder("{}".getBytes(UTF_8))).isEmpty();

		verify(integrationMock).createReminder("{}".getBytes(UTF_8));
		verifyNoMoreInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"reminderId\": 7}]", "\"text\"", "42", "null", "{not json"
	})
	void createReminderWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.createReminder(body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void updateReminderSendsTheBodyUnchanged() {
		service.updateReminder(7, REMINDER_UPDATE_BODY);

		verify(integrationMock).updateReminder(REMINDER_UPDATE_BODY);
		verifyNoMoreInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"reminderId\": 8}", "{\"text\": \"x\"}", "{\"reminderId\": \"7\"}", "{\"reminderId\": null}", "{\"reminderId\": 7.5}", "{\"reminderId\": 7.0}",
		"{\"reminderId\": 99999999999999999999}", "{\"reminderId\": true}", "{\"nested\": {\"reminderId\": 7}}",
		"{\"reminderId\": 7, \"ReminderId\": 8}", "{\"REMINDERID\": 8, \"reminderId\": 7}"
	})
	void updateReminderWhoseBodyDoesNotNameTheSameReminder(final String body) {
		assertThatThrownBy(() -> service.updateReminder(7, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, "The body's reminderId must be 7");

		verifyNoInteractions(integrationMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"[{\"reminderId\": 7}]", "\"text\"", "42", "null", "{not json"
	})
	void updateReminderWithABodyThatIsNotAJsonObject(final String body) {
		assertThatThrownBy(() -> service.updateReminder(7, body.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);

		verifyNoInteractions(integrationMock);
	}

	@Test
	void removeReminder() {
		service.removeReminder(7);

		verify(integrationMock).removeReminder(7);
		verifyNoMoreInteractions(integrationMock);
	}

	private static void assertBadBody(final org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatThrownBy(call)
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_REQUEST, BODY_NOT_A_JSON_OBJECT);
	}
}
