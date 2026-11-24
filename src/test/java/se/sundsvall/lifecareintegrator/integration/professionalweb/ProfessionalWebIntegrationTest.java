package se.sundsvall.lifecareintegrator.integration.professionalweb;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebPrintProperties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebIntegration.ASKED_AGAIN;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebIntegration.NOT_A_PDF;

@ExtendWith(MockitoExtension.class)
class ProfessionalWebIntegrationTest {

	private static final byte[] LIFECARE_JSON = "{\"id\":42,\"personnummer\":\"191212121212\",\"rows\":[{\"amount\":12.50}]}".getBytes(UTF_8);
	private static final byte[] BODY = "{ \"id\": 7,\n \"text\": \"Påminnelse åäö\", \"unknown\": {\"a\": [1, 2.50]} }".getBytes(UTF_8);
	private static final byte[] PDF = "%PDF-1.4\nbinaryå".getBytes(UTF_8);
	private static final String PARAMETER_PAGE = """
		<html><body><form id="myForm" method="post" action="">
		  <input type="hidden" name="X-LEGACY-TOKEN" value="page-token">
		  <label>Visa medsökande</label>
		  <input type="checkbox" name="51_0_2_1" value="true" checked>
		  <input type="hidden" name="51_0_2_1" value="false">
		  <input type="checkbox" name="18_0_2_1" value="true">
		  <input type="hidden" name="18_0_2_1" value="false">
		  <input type="checkbox" name="checked_without_value" checked>
		  <input type="radio" name="choice" value="a">
		  <input type="radio" name="choice" value="b" checked>
		  <input type="text" name="disabled_field" value="x" disabled>
		  <select name="language"><option value="en">English</option><option value="sv" selected>Svenska</option></select>
		  <select name="first"><option>Första</option><option>Andra</option></select>
		  <textarea name="note">Rad åäö &amp; mer</textarea>
		  <input type="submit" name="ok" value="Klar">
		  <input type="text" value="no name">
		</form></body></html>
		""";
	private static final String SUBMITTED_FORM = "X-LEGACY-TOKEN=page-token&51_0_2_1=true&51_0_2_1=false&18_0_2_1=false&checked_without_value=on&choice=b"
		+ "&language=sv&first=F%C3%B6rsta&note=Rad+%C3%A5%C3%A4%C3%B6+%26+mer";

	@Mock
	private ProfessionalWebClient clientMock;

	@Captor
	private ArgumentCaptor<Map<String, String>> queryCaptor;

	private ProfessionalWebIntegration integration;

	@BeforeEach
	void setUp() {
		integration = new ProfessionalWebIntegration(clientMock, new ProfessionalWebPrintProperties("decision-template", "calculation-template", "document-template"));
	}

	@Test
	void getCalculationProposalAsksForTheServiceAsAnInsats() {
		when(clientMock.getCalculationProposalForService(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getCalculationProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getCalculationProposalForService(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getLatestCalculationListsOnlyTheLatestOfTheService() {
		when(clientMock.listCalculations(8, 42, 0, 42, true)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getLatestCalculation(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).listCalculations(8, 42, 0, 42, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationAsksForTheIdAsACalculation() {
		when(clientMock.getCalculation(3, 7)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getCalculation(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getCalculation(3, 7);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationForEditAsksForTheIdAsACalculation() {
		when(clientMock.getCalculationForEdit(3, 7)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getCalculationForEdit(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getCalculationForEdit(3, 7);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void placePersonsSendsTheBodyUnchanged() {
		when(clientMock.placePersons(BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.placePersons(BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).placePersons(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationAmountSendsTheBodyUnchanged() {
		when(clientMock.getCalculationAmount(BODY)).thenReturn("{\"amount\":1250.50}".getBytes(UTF_8));

		assertThat(new String(integration.getCalculationAmount(BODY), UTF_8)).isEqualTo("{\"amount\":1250.50}");

		verify(clientMock).getCalculationAmount(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationSharedCostSendsTheBodyUnchanged() {
		when(clientMock.getCalculationSharedCost(BODY)).thenReturn("1250.50".getBytes(UTF_8));

		assertThat(new String(integration.getCalculationSharedCost(BODY), UTF_8)).isEqualTo("1250.50");

		verify(clientMock).getCalculationSharedCost(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationJobStimuliSendsTheBodyUnchanged() {
		when(clientMock.getJobStimuliForCalculation(BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getCalculationJobStimuli(BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJobStimuliForCalculation(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createCalculationSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.createCalculation(8, 42, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.createCalculation(42, BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).createCalculation(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateCalculationSendsTheBodyUnchangedForTheIdAsACalculation() {
		when(clientMock.updateCalculation(3, 7, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.updateCalculation(7, BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).updateCalculation(3, 7, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void deleteCalculationSendsAnEmptyBodyForTheIdAsACalculation() {
		integration.deleteCalculation(7);

		verify(clientMock).deleteCalculation(3, 7, new byte[0]);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJobStimulusAsksForTheServiceAsAnInsats() {
		when(clientMock.getJobStimulusForService(8, 42)).thenReturn("{\"periods\":[]}".getBytes(UTF_8));

		assertThat(new String(integration.getJobStimulus(42), UTF_8)).isEqualTo("{\"periods\":[]}");

		verify(clientMock).getJobStimulusForService(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJobStimulusEndDateAsksWithTheIsoDate() {
		when(clientMock.getJobStimulusToDate("2026-01-05")).thenReturn("\"2028-01-04\"".getBytes(UTF_8));

		assertThat(new String(integration.getJobStimulusEndDate(LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("\"2028-01-04\"");

		verify(clientMock).getJobStimulusToDate("2026-01-05");
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void saveJobStimulusSendsTheBodyUnchanged() {
		when(clientMock.saveJobStimulus(BODY)).thenReturn("{\"saved\":true}".getBytes(UTF_8));

		assertThat(new String(integration.saveJobStimulus(BODY), UTF_8)).isEqualTo("{\"saved\":true}");

		verify(clientMock).saveJobStimulus(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionProposalAsksForTheServiceAsAnInsatsWithProposalIdZero() {
		when(clientMock.getDecisionProposalForService(8, 42, "TotalSum", 7, 0)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getDecisionProposal(42, "TotalSum", 7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDecisionProposalForService(8, 42, "TotalSum", 7, 0);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionProposalPassesAnEmptyAmountTypeAndCalculationIdZeroAsTheyAre() {
		when(clientMock.getDecisionProposalForService(8, 42, "", 0, 0)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getDecisionProposal(42, "", 0)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDecisionProposalForService(8, 42, "", 0, 0);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionReasonsAsksWithTheDecisionCode() {
		when(clientMock.getMappedDecisionReasons(5)).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(integration.getDecisionReasons(5), UTF_8)).isEqualTo("[]");

		verify(clientMock).getMappedDecisionReasons(5);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionAsksForTheIdAsADecision() {
		when(clientMock.getDecision(4, 9)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getDecision(9)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDecision(4, 9);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createDecisionSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.createDecision(8, 42, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.createDecision(42, BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).createDecision(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateDecisionSendsTheBodyUnchangedForTheIdAsADecision() {
		when(clientMock.updateDecision(4, 9, BODY)).thenReturn(LIFECARE_JSON);

		assertThat(integration.updateDecision(9, BODY)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).updateDecision(4, 9, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentsAsksWithThePersonnummer() {
		when(clientMock.getDocumentsListForClient("191212121212")).thenReturn("[]".getBytes(UTF_8));

		assertThat(new String(integration.getDocuments("191212121212"), UTF_8)).isEqualTo("[]");

		verify(clientMock).getDocumentsListForClient("191212121212");
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJournalNoteReadsForDisplay() {
		when(clientMock.getJournalNoteWithContent(1, true, false)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getJournalNote(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJournalNoteWithContent(1, true, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getJournalNoteForEditReadsWithRevisionsInEdit() {
		when(clientMock.getJournalNoteWithContent(1, false, true)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getJournalNoteForEdit(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getJournalNoteWithContent(1, false, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentReadsForDisplay() {
		when(clientMock.getDocumentWithContent(1, true, false)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getDocument(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDocumentWithContent(1, true, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentForEditReadsWithRevisionsInEdit() {
		when(clientMock.getDocumentWithContent(1, false, true)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getDocumentForEdit(1)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getDocumentWithContent(1, false, true);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void documentProposalsAskForTheService() {
		when(clientMock.getNoteProposalForService(42)).thenReturn("{\"note\":1}".getBytes(UTF_8));
		when(clientMock.getDocumentProposalForService(42)).thenReturn("{\"document\":1}".getBytes(UTF_8));

		assertThat(new String(integration.getJournalNoteProposal(42), UTF_8)).isEqualTo("{\"note\":1}");
		assertThat(new String(integration.getDocumentProposal(42), UTF_8)).isEqualTo("{\"document\":1}");

		verify(clientMock).getNoteProposalForService(42);
		verify(clientMock).getDocumentProposalForService(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void documentWritesSendTheBodyUnchanged() {
		when(clientMock.createJournalNote(BODY)).thenReturn("{\"documentId\":1}".getBytes(UTF_8));
		when(clientMock.createDocument(BODY)).thenReturn("{\"documentId\":2}".getBytes(UTF_8));
		when(clientMock.updateJournalNote(BODY)).thenReturn(LIFECARE_JSON);
		when(clientMock.updateDocument(BODY)).thenReturn(new byte[0]);

		assertThat(new String(integration.createJournalNote(BODY), UTF_8)).isEqualTo("{\"documentId\":1}");
		assertThat(new String(integration.createDocument(BODY), UTF_8)).isEqualTo("{\"documentId\":2}");
		assertThat(integration.updateJournalNote(BODY)).isEqualTo(LIFECARE_JSON);
		assertThat(integration.updateDocument(BODY)).isEmpty();

		verify(clientMock).createJournalNote(BODY);
		verify(clientMock).createDocument(BODY);
		verify(clientMock).updateJournalNote(BODY);
		verify(clientMock).updateDocument(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getPaymentProposalAsksForTheServiceAsAnInsats() {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getPaymentProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getPaymentForCreate(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getLatestPaymentsAsksForTheServiceAsAnInsats() {
		when(clientMock.getLatestPayments(8, 42)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getLatestPayments(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getLatestPayments(8, 42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void hasHouseholdAsksWithThePersonLifecareNamesForTheServiceAndTheIsoDate() {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn("{\"payment\":{\"susPersonId\":\"191212121212\",\"amount\":0},\"payees\":[]}".getBytes(UTF_8));
		when(clientMock.hasHouseholdThisDate("191212121212", "2026-01-05")).thenReturn("false".getBytes(UTF_8));

		assertThat(new String(integration.hasHousehold(42, LocalDate.of(2026, 1, 5)), UTF_8)).isEqualTo("false");

		verify(clientMock).getPaymentForCreate(8, 42);
		verify(clientMock).hasHouseholdThisDate("191212121212", "2026-01-05");
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"{\"payment\":{\"susPersonId\":\"\"}}", "{\"payment\":{\"susPersonId\":\"  \"}}", "{\"payment\":{\"susPersonId\":null}}",
		"{\"payment\":{}}", "{\"payment\":null}", "{}", "[]", "not json", ""
	})
	void hasHouseholdWhenLifecareNamesNoPersonForTheServiceIsBadGateway(final String paymentForCreate) {
		when(clientMock.getPaymentForCreate(8, 42)).thenReturn(paymentForCreate.getBytes(UTF_8));

		assertThatThrownBy(() -> integration.hasHousehold(42, LocalDate.of(2026, 1, 5)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, "Lifecare named no person for the service");

		verify(clientMock, never()).hasHouseholdThisDate(anyString(), anyString());
	}

	@Test
	void createPaymentSendsTheBodyUnchangedForTheServiceAsAnInsats() {
		when(clientMock.createPayment(8, 42, BODY)).thenReturn("{\"paymentId\":5}".getBytes(UTF_8));

		assertThat(new String(integration.createPayment(42, BODY), UTF_8)).isEqualTo("{\"paymentId\":5}");

		verify(clientMock).createPayment(8, 42, BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void createPayeeSendsTheBodyUnchanged() {
		when(clientMock.createPayee(BODY)).thenReturn("{\"payeeId\":3}".getBytes(UTF_8));

		assertThat(new String(integration.createPayee(BODY), UTF_8)).isEqualTo("{\"payeeId\":3}");

		verify(clientMock).createPayee(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCalculationPdfAsksForThePrintContainerOfTheCalculation() {
		when(clientMock.renderPrint(eq("PrintContainer"), anyMap())).thenReturn(PDF);

		assertThat(integration.getCalculationPdf(7)).isEqualTo(PDF);

		verify(clientMock).renderPrint(eq("PrintContainer"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "calculation-template"), entry("ownerType", "BERAK"),
			entry("ownerCode", "999999999"), entry("objectId", "7"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDecisionPdfAsksForTheDecisionPrintWithoutRevisions() {
		when(clientMock.renderPrint(eq("PrintDecision"), anyMap())).thenReturn(PDF);

		assertThat(integration.getDecisionPdf(9)).isEqualTo(PDF);

		verify(clientMock).renderPrint(eq("PrintDecision"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "decision-template"), entry("decisionId", "9"), entry("hideRevisions", "true"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getDocumentPdfAsksForTheDocumentPrintWithoutRevisions() {
		when(clientMock.renderPrint(eq("PrintDocument"), anyMap())).thenReturn(PDF);

		assertThat(integration.getDocumentPdf(1)).isEqualTo(PDF);

		verify(clientMock).renderPrint(eq("PrintDocument"), queryCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "document-template"), entry("documentId", "1"), entry("hideRevisions", "true"));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void parameterQuestionsAreAnsweredWithLifecaresDefaultsToTheSamePrintAndQuery() {
		when(clientMock.renderPrint(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn(PDF);

		assertThat(integration.getDecisionPdf(9)).isEqualTo(PDF);

		final var formCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(clientMock).renderPrint(eq("PrintDecision"), anyMap());
		verify(clientMock).submitPrintParameters(eq("PrintDecision"), queryCaptor.capture(), formCaptor.capture());
		assertThat(queryCaptor.getValue()).containsExactly(entry("templateId", "decision-template"), entry("decisionId", "9"), entry("hideRevisions", "true"));
		assertThat(new String(formCaptor.getValue(), UTF_8)).isEqualTo(SUBMITTED_FORM);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void parameterQuestionsAskedAgainAfterTheFormIsBadGateway() {
		when(clientMock.renderPrint(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));

		assertThatThrownBy(() -> integration.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, ASKED_AGAIN);

		verify(clientMock).renderPrint(eq("PrintDecision"), anyMap());
		verify(clientMock).submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void somethingElseThanAPdfAfterTheFormIsBadGateway() {
		when(clientMock.renderPrint(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenReturn("<html>Fel</html>".getBytes(UTF_8));

		assertThatThrownBy(() -> integration.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).renderPrint(eq("PrintDecision"), anyMap());
		verify(clientMock).submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", "{\"pdf\":false}", "<html><form id=\"myForm\"></form></html>", "<html><input name=\"X-LEGACY-TOKEN\"></html>", "%PD"
	})
	void neitherAPdfNorParameterQuestionsIsBadGatewayWithoutAForm(final String answer) {
		when(clientMock.renderPrint(eq("PrintDocument"), anyMap())).thenReturn(answer.getBytes(UTF_8));

		assertThatThrownBy(() -> integration.getDocumentPdf(1))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).renderPrint(eq("PrintDocument"), anyMap());
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void noAnswerAtAllIsBadGateway() {
		when(clientMock.renderPrint(eq("PrintDocument"), anyMap())).thenReturn(null);

		assertThatThrownBy(() -> integration.getDocumentPdf(1))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, NOT_A_PDF);

		verify(clientMock).renderPrint(eq("PrintDocument"), anyMap());
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void formRefusedByLifecareIsLifecaresRefusal() {
		when(clientMock.renderPrint(eq("PrintDecision"), anyMap())).thenReturn(PARAMETER_PAGE.getBytes(UTF_8));
		when(clientMock.submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class))).thenThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Mallen saknas"));

		assertThatThrownBy(() -> integration.getDecisionPdf(9))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(UNPROCESSABLE_CONTENT, "Mallen saknas");

		verify(clientMock).renderPrint(eq("PrintDecision"), anyMap());
		verify(clientMock).submitPrintParameters(eq("PrintDecision"), anyMap(), any(byte[].class));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void listRemindersAsksForTheService() {
		when(clientMock.listRemindersByServiceId(42)).thenReturn(LIFECARE_JSON);

		assertThat(integration.listReminders(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).listRemindersByServiceId(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getReminderProposalAsksForTheService() {
		when(clientMock.getReminderProposalForService(42)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getReminderProposal(42)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getReminderProposalForService(42);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getReminderReadsTheEditCompositeWithoutOptions() {
		when(clientMock.getReminderEditComposite(7, false)).thenReturn(LIFECARE_JSON);

		assertThat(integration.getReminder(7)).isEqualTo(LIFECARE_JSON);

		verify(clientMock).getReminderEditComposite(7, false);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void reminderWritesSendTheBodyUnchanged() {
		when(clientMock.createReminder(BODY)).thenReturn("{\"id\":9}".getBytes(UTF_8));

		assertThat(new String(integration.createReminder(BODY), UTF_8)).isEqualTo("{\"id\":9}");
		integration.updateReminder(BODY);

		verify(clientMock).createReminder(BODY);
		verify(clientMock).updateReminder(BODY);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void removeReminderSendsTheId() {
		integration.removeReminder(7);

		verify(clientMock).removeReminder("{\"id\":7}".getBytes(UTF_8));
		verifyNoMoreInteractions(clientMock);
	}
}
