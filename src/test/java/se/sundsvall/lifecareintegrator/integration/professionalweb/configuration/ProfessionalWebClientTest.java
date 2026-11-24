package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ProfessionalWebClientTest {

	private static final String API2 = "/WESE.FC.ProfessionalWeb/api2/";
	private static final String CALCULATION = API2 + "Calculation/";
	private static final String DECISION = API2 + "Decision/";
	private static final String DOCUMENT = API2 + "Document/";
	private static final String CREATE_PAYMENT = API2 + "Payment/Create?businessType=8&businessId=42";
	private static final String PRINT_DECISION = "/WESE.FC.ProfessionalWeb/RenderPdf/PrintDecision";
	private static final String QUERY = "?templateId=885bfb68-c97b-47c0-921d-ef00caaa2423&decisionId=9&hideRevisions=true";
	private static final String HEARTBEAT = "/WESE.FC.ProfessionalWeb/Heartbeat";
	private static final String CALCULATION_BODY = "{ \"personId\": \"19121212-1212\",\n \"text\": \"åäö\", \"unknown\": {\"a\": [1, 2.50]} }";
	private static final String DECISION_JSON = "{\"decisionId\":9,\"personnummer\":\"191212121212\",\"unknown\":[1]}";
	private static final String DECISION_BODY = "{ \"decisionId\": 9,\n \"text\": \"Beslut åäö\", \"unknown\": {\"a\": [1, 2.50]} }";
	private static final String DOCUMENT_JSON = "{\"documentId\":1,\"content\":\"Anteckning åäö\",\"personnummer\":\"191212121212\"}";
	private static final String DOCUMENT_BODY = "{ \"documentId\": 1,\n \"content\": \"Anteckning åäö\", \"unknown\": {\"a\": [1, 2.50]} }";
	private static final String PAYMENT_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}";
	private static final String PAYMENT_BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }";
	private static final String REMINDER_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}";
	private static final byte[] PDF = "%PDF-1.4\n%âãÏÓ\nbinary".getBytes(UTF_8);
	private static final String PARAMETER_PAGE = """
		<html><body><form id="myForm" method="post">
		  <input type="hidden" name="X-LEGACY-TOKEN" value="page-token">
		  <input type="checkbox" name="51_0_2_1" value="true" checked>
		  <input type="hidden" name="51_0_2_1" value="false">
		</form></body></html>
		""";

	private WireMockServer wireMock;
	private ProfessionalWebClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebClient.class, wireMock.baseUrl());
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void jobStimulusToDateIsReadWithTheDateAsId() {
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetJobStimulusToDate?id=2026-11-01")).willReturn(okJson("\"2028-10-31\"")));

		assertThat(new String(client.getJobStimulusToDate("2026-11-01"), UTF_8)).isEqualTo("\"2028-10-31\"");

		wireMock.verify(1, getRequestedFor(urlEqualTo(CALCULATION + "GetJobStimulusToDate?id=2026-11-01"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void saveJobStimulusPostsTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "SaveJobStimulus")).willReturn(okJson("{\"saved\":true}")));

		final var answer = client.saveJobStimulus(CALCULATION_BODY.getBytes(UTF_8));

		assertThat(new String(answer, UTF_8)).isEqualTo("{\"saved\":true}");
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "SaveJobStimulus"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(CALCULATION_BODY.getBytes(UTF_8))));
	}

	@Test
	void saveJobStimulusWithAnEmptyAnswerReturnsNoBytes() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "SaveJobStimulus")).willReturn(aResponse().withStatus(204)));

		assertThat(client.saveJobStimulus("{}".getBytes(UTF_8))).isEmpty();
	}

	@Test
	void saveJobStimulusRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "SaveJobStimulus"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Perioden överlappar\"}")));

		assertThatThrownBy(() -> client.saveJobStimulus("{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Perioden överlappar");
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "SaveJobStimulus")));
	}

	@Test
	void saveJobStimulusFailingInLifecareIsBadGatewayWithStatusAndText() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "SaveJobStimulus"))
			.willReturn(aResponse().withStatus(500).withBody("{\"message\":\"Något gick fel\"}")));

		assertThatThrownBy(() -> client.saveJobStimulus("{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered with status 500: Något gick fel");
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "SaveJobStimulus")));
	}

	@Test
	void calculationReadsAreMadeWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetProposalService?businessType=8&businessId=42")).willReturn(okJson("{\"proposal\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetCalculation?businessType=3&businessId=7")).willReturn(okJson("{\"calculation\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetCalculationForEdit?businessType=3&businessId=7")).willReturn(okJson("{\"edit\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetJobStimulusForService?businessType=8&businessId=42")).willReturn(okJson("{\"jobStimulus\":1}")));

		assertThat(new String(client.getCalculationProposalForService(8, 42), UTF_8)).isEqualTo("{\"proposal\":1}");
		assertThat(new String(client.getCalculation(3, 7), UTF_8)).isEqualTo("{\"calculation\":1}");
		assertThat(new String(client.getCalculationForEdit(3, 7), UTF_8)).isEqualTo("{\"edit\":1}");
		assertThat(new String(client.getJobStimulusForService(8, 42), UTF_8)).isEqualTo("{\"jobStimulus\":1}");

		wireMock.verify(1, getRequestedFor(urlEqualTo(CALCULATION + "GetProposalService?businessType=8&businessId=42")));
		wireMock.verify(1, getRequestedFor(urlEqualTo(CALCULATION + "GetCalculation?businessType=3&businessId=7")));
		wireMock.verify(1, getRequestedFor(urlEqualTo(CALCULATION + "GetCalculationForEdit?businessType=3&businessId=7")));
		wireMock.verify(1, getRequestedFor(urlEqualTo(CALCULATION + "GetJobStimulusForService?businessType=8&businessId=42")));
	}

	@Test
	void listCalculationsIsReadWithItsParametersInOrder() {
		final var url = CALCULATION + "ListCalculations?businessType=8&businessId=42&investigationId=0&serviceId=42&onlylatest=true";
		wireMock.stubFor(get(urlEqualTo(url)).willReturn(okJson("[]")));

		assertThat(new String(client.listCalculations(8, 42, 0, 42, true), UTF_8)).isEqualTo("[]");

		wireMock.verify(1, getRequestedFor(urlEqualTo(url)));
	}

	@Test
	void reckoningsPostTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "PlacePersons")).willReturn(okJson("{\"placed\":1}")));
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "GetAmount")).willReturn(okJson("{\"amount\":12.50}")));
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "GetSharedCost")).willReturn(okJson("1250.50")));
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "GetJobStimuliForCalculation")).willReturn(okJson("{\"stimuli\":1}")));

		assertThat(new String(client.placePersons(CALCULATION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"placed\":1}");
		assertThat(new String(client.getCalculationAmount(CALCULATION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"amount\":12.50}");
		assertThat(new String(client.getCalculationSharedCost(CALCULATION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("1250.50");
		assertThat(new String(client.getJobStimuliForCalculation(CALCULATION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"stimuli\":1}");

		for (final var operation : java.util.List.of("PlacePersons", "GetAmount", "GetSharedCost", "GetJobStimuliForCalculation")) {
			wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + operation))
				.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
				.withRequestBody(binaryEqualTo(CALCULATION_BODY.getBytes(UTF_8))));
		}
	}

	@Test
	void createAndUpdateCalculationPostTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "Create?businessType=8&businessId=42")).willReturn(okJson("{\"calculationId\":7}")));
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "Update?businessType=3&businessId=7")).willReturn(aResponse().withStatus(204)));

		assertThat(new String(client.createCalculation(8, 42, CALCULATION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"calculationId\":7}");
		assertThat(client.updateCalculation(3, 7, CALCULATION_BODY.getBytes(UTF_8))).isEmpty();

		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "Create?businessType=8&businessId=42")).withRequestBody(binaryEqualTo(CALCULATION_BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "Update?businessType=3&businessId=7")).withRequestBody(binaryEqualTo(CALCULATION_BODY.getBytes(UTF_8))));
	}

	@Test
	void deleteCalculationIsADeleteWithAnEmptyBody() {
		wireMock.stubFor(delete(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7")).willReturn(aResponse().withStatus(200)));

		client.deleteCalculation(3, 7, new byte[0]);

		wireMock.verify(1, deleteRequestedFor(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7"))
			.withHeader("Content-Length", equalTo("0"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8")));
	}

	@Test
	void deleteCalculationRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(delete(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beräkningen är slutlig\"}")));

		assertThatThrownBy(() -> client.deleteCalculation(3, 7, new byte[0]))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beräkningen är slutlig");
	}

	@Test
	void decisionProposalForServiceIsReadWithItsParametersInOrder() {
		final var url = DECISION + "GetProposalForService?businessType=8&businessId=42&amountType=TotalSum&calculationId=7&proposalId=0";
		wireMock.stubFor(get(urlEqualTo(url)).willReturn(okJson(DECISION_JSON)));

		assertThat(new String(client.getDecisionProposalForService(8, 42, "TotalSum", 7, 0), UTF_8)).isEqualTo(DECISION_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(url)).withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void decisionProposalForServiceWithoutACalculationSendsTheEmptyAmountType() {
		final var url = DECISION + "GetProposalForService?businessType=8&businessId=42&amountType=&calculationId=0&proposalId=0";
		wireMock.stubFor(get(urlEqualTo(url)).willReturn(okJson(DECISION_JSON)));

		assertThat(new String(client.getDecisionProposalForService(8, 42, "", 0, 0), UTF_8)).isEqualTo(DECISION_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(url)));
	}

	@Test
	void mappedDecisionReasonsAreReadWithTheDecisionCodeAsId() {
		wireMock.stubFor(get(urlEqualTo(DECISION + "GetMappedDecisionReasons?id=5")).willReturn(okJson("[]")));

		assertThat(new String(client.getMappedDecisionReasons(5), UTF_8)).isEqualTo("[]");

		wireMock.verify(1, getRequestedFor(urlEqualTo(DECISION + "GetMappedDecisionReasons?id=5")));
	}

	@Test
	void decisionIsReadWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(DECISION + "GetDecision?businessType=4&businessId=9")).willReturn(okJson(DECISION_JSON)));

		assertThat(new String(client.getDecision(4, 9), UTF_8)).isEqualTo(DECISION_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DECISION + "GetDecision?businessType=4&businessId=9")));
	}

	@Test
	void createDecisionPostsTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Create?businessType=8&businessId=42")).willReturn(okJson(DECISION_JSON)));

		assertThat(new String(client.createDecision(8, 42, DECISION_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo(DECISION_JSON);

		wireMock.verify(1, postRequestedFor(urlEqualTo(DECISION + "Create?businessType=8&businessId=42"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(DECISION_BODY.getBytes(UTF_8))));
	}

	@Test
	void updateDecisionIsAPostWithTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Update?businessType=4&businessId=9")).willReturn(aResponse().withStatus(204)));

		assertThat(client.updateDecision(4, 9, DECISION_BODY.getBytes(UTF_8))).isEmpty();

		wireMock.verify(1, postRequestedFor(urlEqualTo(DECISION + "Update?businessType=4&businessId=9"))
			.withRequestBody(binaryEqualTo(DECISION_BODY.getBytes(UTF_8))));
	}

	@Test
	void updateDecisionRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Update?businessType=4&businessId=9"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beslutet är verkställt\"}")));

		assertThatThrownBy(() -> client.updateDecision(4, 9, "{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beslutet är verkställt");
	}

	@Test
	void documentsListIsReadWithATrailingSlashAndThePersonnummerAsId() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentsListForClient/?id=191212121212")).willReturn(okJson("[]")));

		assertThat(new String(client.getDocumentsListForClient("191212121212"), UTF_8)).isEqualTo("[]");

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentsListForClient/?id=191212121212"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void journalNoteIsReadWithIdHideRevisionsAndInEditInOrder() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetJournalNoteWithContent/?id=1&hideRevisions=true&inEdit=false")).willReturn(okJson(DOCUMENT_JSON)));

		assertThat(new String(client.getJournalNoteWithContent(1, true, false), UTF_8)).isEqualTo(DOCUMENT_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetJournalNoteWithContent/?id=1&hideRevisions=true&inEdit=false")));
	}

	@Test
	void documentIsReadWithIdHideRevisionsAndInEditInOrder() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentWithContent/?id=1&hideRevisions=false&inEdit=true")).willReturn(okJson(DOCUMENT_JSON)));

		assertThat(new String(client.getDocumentWithContent(1, false, true), UTF_8)).isEqualTo(DOCUMENT_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentWithContent/?id=1&hideRevisions=false&inEdit=true")));
	}

	@Test
	void documentProposalsAreReadWithoutATrailingSlashAndTheServiceAsId() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetNoteProposalForService?id=42")).willReturn(okJson("{\"note\":1}")));
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentProposalForService?id=42")).willReturn(okJson("{\"document\":1}")));

		assertThat(new String(client.getNoteProposalForService(42), UTF_8)).isEqualTo("{\"note\":1}");
		assertThat(new String(client.getDocumentProposalForService(42), UTF_8)).isEqualTo("{\"document\":1}");

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetNoteProposalForService?id=42")));
		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentProposalForService?id=42")));
	}

	@Test
	void documentCreatesPostTheBodyByteForByteWithATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "CreateJournalNote/")).willReturn(okJson("{\"documentId\":1}")));
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "CreateDocument/")).willReturn(okJson("{\"documentId\":2}")));

		assertThat(new String(client.createJournalNote(DOCUMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"documentId\":1}");
		assertThat(new String(client.createDocument(DOCUMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"documentId\":2}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "CreateJournalNote/"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(DOCUMENT_BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "CreateDocument/"))
			.withRequestBody(binaryEqualTo(DOCUMENT_BODY.getBytes(UTF_8))));
	}

	@Test
	void documentUpdatesArePostsOfTheBodyByteForByteWithATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateJournalNote/")).willReturn(aResponse().withStatus(204)));
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateDocument/")).willReturn(okJson(DOCUMENT_JSON)));

		assertThat(client.updateJournalNote(DOCUMENT_BODY.getBytes(UTF_8))).isEmpty();
		assertThat(new String(client.updateDocument(DOCUMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo(DOCUMENT_JSON);

		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "UpdateJournalNote/")).withRequestBody(binaryEqualTo(DOCUMENT_BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "UpdateDocument/")).withRequestBody(binaryEqualTo(DOCUMENT_BODY.getBytes(UTF_8))));
	}

	@Test
	void updateDocumentRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateDocument/"))
			.willReturn(aResponse().withStatus(461).withBody("{\"Message\":\"Dokumentet är låst\"}")));

		assertThatThrownBy(() -> client.updateDocument("{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Dokumentet är låst");
	}

	@Test
	void paymentForCreateIsReadWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(API2 + "Payment/GetPaymentForCreate?businessType=8&businessId=42")).willReturn(okJson(PAYMENT_JSON)));

		assertThat(new String(client.getPaymentForCreate(8, 42), UTF_8)).isEqualTo(PAYMENT_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(API2 + "Payment/GetPaymentForCreate?businessType=8&businessId=42"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void latestPaymentsAreReadWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(API2 + "Payment/GetLatestPayments?businessType=8&businessId=42")).willReturn(okJson("[]")));

		assertThat(new String(client.getLatestPayments(8, 42), UTF_8)).isEqualTo("[]");

		wireMock.verify(1, getRequestedFor(urlEqualTo(API2 + "Payment/GetLatestPayments?businessType=8&businessId=42")));
	}

	@Test
	void householdIsAskedWithATrailingSlashThenPersonIdAndDate() {
		wireMock.stubFor(get(urlEqualTo(API2 + "Household/HasHouseholdThisDate/?personId=191212121212&date=2026-11-01")).willReturn(okJson("true")));

		assertThat(new String(client.hasHouseholdThisDate("191212121212", "2026-11-01"), UTF_8)).isEqualTo("true");

		wireMock.verify(1, getRequestedFor(urlEqualTo(API2 + "Household/HasHouseholdThisDate/?personId=191212121212&date=2026-11-01")));
	}

	@Test
	void createPaymentPostsTheBodyByteForByteWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42")).willReturn(okJson("{\"paymentId\":5}")));

		assertThat(new String(client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"paymentId\":5}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(PAYMENT_BODY.getBytes(UTF_8))));
	}

	@Test
	void createPaymentFailingInLifecareIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.willReturn(aResponse().withStatus(500).withBody("{\"message\":\"Fel\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered with status 500: Fel");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentThatTimesOutIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.willReturn(okJson("{}").withFixedDelay(7000)));

		assertThatThrownBy(() -> client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare could not be reached");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentRefusedForAMissingSessionIsSentAgainOnceTheSessionIsBack() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT)).inScenario("s").whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(360)).willSetStateTo("bootstrapped"));
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT)).inScenario("s").whenScenarioStateIs("bootstrapped")
			.willReturn(okJson("{\"paymentId\":5}")));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));

		assertThat(new String(client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"paymentId\":5}");

		wireMock.verify(2, postRequestedFor(urlEqualTo(CREATE_PAYMENT)).withRequestBody(binaryEqualTo(PAYMENT_BODY.getBytes(UTF_8))));
	}

	@Test
	void createPaymentRefusedByLifecareIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beloppet saknas\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beloppet saknas");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentAnsweredWithBadRequestIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(aResponse().withStatus(400).withBody("{\"Message\":\"Felaktig utbetalning\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(400, "Felaktig utbetalning");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentAnsweredWithAnHtmlPageIsBadGatewayAndNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(ok("<html>Logga in</html>").withHeader("Content-Type", "text/html; charset=utf-8")));

		assertThatThrownBy(() -> client.createPayment(8, 42, PAYMENT_BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered a write with an HTML page");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPayeePostsTheBodyByteForByteWithoutATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payee/Create")).willReturn(okJson("{\"payeeId\":3}")));

		assertThat(new String(client.createPayee(PAYMENT_BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"payeeId\":3}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(API2 + "Payee/Create"))
			.withRequestBody(binaryEqualTo(PAYMENT_BODY.getBytes(UTF_8))));
	}

	@Test
	void createPayeeRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payee/Create"))
			.willReturn(aResponse().withStatus(461).withBody("{\"ExceptionMessage\":\"Kontot finns redan\"}")));

		assertThatThrownBy(() -> client.createPayee("{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Kontot finns redan");
	}

	@Test
	void reminderProposalForServiceIsReadWithTheServiceId() {
		wireMock.stubFor(get(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/GetProposalForService?id=42")).willReturn(okJson(REMINDER_JSON)));

		assertThat(new String(client.getReminderProposalForService(42), UTF_8)).isEqualTo(REMINDER_JSON);
		wireMock.verify(1, getRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/GetProposalForService?id=42")));
	}

	@Test
	void reminderEditCompositeIsReadWithTheReminderIdThenReadOptionsFalse() {
		wireMock.stubFor(get(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/GetReminderEditComposite?reminderId=7&readOptions=false"))
			.willReturn(okJson(REMINDER_JSON)));

		assertThat(new String(client.getReminderEditComposite(7, false), UTF_8)).isEqualTo(REMINDER_JSON);
		wireMock.verify(1, getRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/GetReminderEditComposite?reminderId=7&readOptions=false")));
	}

	@Test
	void createReminderPostsTheBodyAsItIsAndReturnsLifecaresAnswer() {
		final var body = "{ \"serviceId\": 42, \"text\": \"Påminnelse åäö\", \"personnummer\": \"191212121212\", \"unknown\": {\"a\": [1]} }";
		wireMock.stubFor(post(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/")).willReturn(okJson("{\"id\":9}")));

		final var answer = client.createReminder(body.getBytes(UTF_8));

		assertThat(new String(answer, UTF_8)).isEqualTo("{\"id\":9}");
		wireMock.verify(1, postRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok"))
			.withRequestBody(binaryEqualTo(body.getBytes(UTF_8))));
	}

	@Test
	void createReminderWithAnEmptyAnswerReturnsNoBytes() {
		wireMock.stubFor(post(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/")).willReturn(ok()));

		assertThat(client.createReminder("{}".getBytes(UTF_8))).isEmpty();
	}

	@Test
	void createReminderAnsweredWithNoContentReturnsNoBytes() {
		wireMock.stubFor(post(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/CreateReminder/")).willReturn(aResponse().withStatus(204)));

		assertThat(client.createReminder("{}".getBytes(UTF_8))).isEmpty();
	}

	@Test
	void updateReminderPostsTheBodyAsItIs() {
		final var body = "{\"reminderId\":7, \"text\":\"Ring\"}";
		wireMock.stubFor(post(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/UpdateReminder/")).willReturn(ok()));

		client.updateReminder(body.getBytes(UTF_8));

		wireMock.verify(1, postRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/UpdateReminder/"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(equalTo(body)));
	}

	@Test
	void removeReminderSendsADeleteWithTheBody() {
		wireMock.stubFor(delete(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/RemoveReminder/")).willReturn(ok()));

		client.removeReminder("{\"id\":7}".getBytes(UTF_8));

		wireMock.verify(1, deleteRequestedFor(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/RemoveReminder/"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(equalTo("{\"id\":7}")));
	}

	@Test
	void updateReminderRefusedByLifecareIsUnprocessable() {
		wireMock.stubFor(post(urlEqualTo("/WESE.FC.ProfessionalWeb/api2/Reminders/UpdateReminder/"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Påminnelsen är klar\"}")));

		assertThatThrownBy(() -> client.updateReminder("{\"reminderId\":7}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Påminnelsen är klar");
	}

	@Test
	void renderPrintAsksForThePrintWithTheQueryInOrderAndReturnsThePdfBytes() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));

		assertThat(client.renderPrint("PrintDecision", decisionQuery())).isEqualTo(PDF);

		wireMock.verify(1, getRequestedFor(urlEqualTo(PRINT_DECISION + QUERY))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void parameterQuestionsAreHandedBackNotTakenForALostSession() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok(PARAMETER_PAGE).withHeader("Content-Type", "text/html; charset=utf-8")));

		assertThat(new String(client.renderPrint("PrintDecision", decisionQuery()), UTF_8)).isEqualTo(PARAMETER_PAGE);

		wireMock.verify(1, getRequestedFor(urlPathEqualTo(PRINT_DECISION)));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void anotherHtmlPageIsStillALostSession() {
		wireMock.stubFor(get(urlPathEqualTo(PRINT_DECISION)).inScenario("s").whenScenarioStateIs(STARTED)
			.willReturn(ok("<html>Logga in</html>").withHeader("Content-Type", "text/html")).willSetStateTo("bootstrapped"));
		wireMock.stubFor(get(urlPathEqualTo(PRINT_DECISION)).inScenario("s").whenScenarioStateIs("bootstrapped")
			.willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));

		assertThat(client.renderPrint("PrintDecision", decisionQuery())).isEqualTo(PDF);

		wireMock.verify(2, getRequestedFor(urlPathEqualTo(PRINT_DECISION)));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void submitPrintParametersPostsTheFormLikeABrowserToTheSamePathAndQuery() {
		final var form = "X-LEGACY-TOKEN=page-token&51_0_2_1=true&51_0_2_1=false";
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));

		assertThat(client.submitPrintParameters("PrintDecision", decisionQuery(), form.getBytes(UTF_8))).isEqualTo(PDF);

		wireMock.verify(1, postRequestedFor(urlEqualTo(PRINT_DECISION + QUERY))
			.withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
			.withHeader("Accept", equalTo("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
			.withHeader("Referer", equalTo(wireMock.baseUrl() + PRINT_DECISION + QUERY))
			.withHeader("Origin", equalTo(wireMock.baseUrl()))
			.withHeader("X-Requested-With", absent())
			.withHeader("Cookie", com.github.tomakehurst.wiremock.client.WireMock.containing("LEGACY-TOKEN=tok"))
			.withRequestBody(equalTo(form)));
	}

	@Test
	void submitPrintParametersSentAgainAfterAMissingSessionKeepsThePrintsAddressAsReferer() {
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).inScenario("s").whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(360)).willSetStateTo("bootstrapped"));
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).inScenario("s").whenScenarioStateIs("bootstrapped")
			.willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));

		assertThat(client.submitPrintParameters("PrintDecision", decisionQuery(), "a=b".getBytes(UTF_8))).isEqualTo(PDF);

		wireMock.verify(2, postRequestedFor(urlEqualTo(PRINT_DECISION + QUERY))
			.withHeader("Referer", equalTo(wireMock.baseUrl() + PRINT_DECISION + QUERY)));
	}

	@Test
	void submitPrintParametersAnsweredWithTheQuestionsAgainHandsThemBack() {
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok(PARAMETER_PAGE).withHeader("Content-Type", "text/html; charset=utf-8")));

		assertThat(new String(client.submitPrintParameters("PrintDecision", decisionQuery(), "a=b".getBytes(UTF_8)), UTF_8)).isEqualTo(PARAMETER_PAGE);

		wireMock.verify(1, postRequestedFor(urlPathEqualTo(PRINT_DECISION)));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void submitPrintParametersAnsweredWithAnotherHtmlPageIsBadGatewayAndNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok("<html>Logga in</html>").withHeader("Content-Type", "text/html")));

		assertThatThrownBy(() -> client.submitPrintParameters("PrintDecision", decisionQuery(), "a=b".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered a write with an HTML page");

		wireMock.verify(1, postRequestedFor(urlPathEqualTo(PRINT_DECISION)));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void submitPrintParametersRefusedByLifecareIsTheSameProblemAsOtherRefusals() {
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Mallen saknas\"}")));

		assertThatThrownBy(() -> client.submitPrintParameters("PrintDecision", decisionQuery(), "a=b".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Mallen saknas");
	}

	@Test
	void renderPrintRefusedByLifecareIsAProblem() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(aResponse().withStatus(404)));

		assertThatThrownBy(() -> client.renderPrint("PrintDecision", decisionQuery()))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value())
			.isEqualTo(404);
	}

	private static Map<String, String> decisionQuery() {
		final var query = new LinkedHashMap<String, String>();
		query.put("templateId", "885bfb68-c97b-47c0-921d-ef00caaa2423");
		query.put("decisionId", "9");
		query.put("hideRevisions", "true");
		return query;
	}
}
