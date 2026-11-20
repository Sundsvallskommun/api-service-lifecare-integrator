package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebCalculationClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ProfessionalWebCalculationClientTest {

	private static final String CALCULATION = "/WESE.FC.ProfessionalWeb/api2/Calculation/";
	private static final String BODY = "{ \"personId\": \"19121212-1212\",\n \"text\": \"åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	private WireMockServer wireMock;
	private ProfessionalWebCalculationClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebCalculationClient.class, wireMock.baseUrl());
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

		final var answer = client.saveJobStimulus(BODY.getBytes(UTF_8));

		assertThat(new String(answer, UTF_8)).isEqualTo("{\"saved\":true}");
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "SaveJobStimulus"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
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
	void readsAreMadeWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetProposalService?businessType=8&businessId=42")).willReturn(okJson("{\"proposal\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetCalculation?businessType=3&businessId=7")).willReturn(okJson("{\"calculation\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetCalculationForEdit?businessType=3&businessId=7")).willReturn(okJson("{\"edit\":1}")));
		wireMock.stubFor(get(urlEqualTo(CALCULATION + "GetJobStimulusForService?businessType=8&businessId=42")).willReturn(okJson("{\"jobStimulus\":1}")));

		assertThat(new String(client.getProposalService(8, 42), UTF_8)).isEqualTo("{\"proposal\":1}");
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

		assertThat(new String(client.placePersons(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"placed\":1}");
		assertThat(new String(client.getAmount(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"amount\":12.50}");
		assertThat(new String(client.getSharedCost(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("1250.50");
		assertThat(new String(client.getJobStimuliForCalculation(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"stimuli\":1}");

		for (final var operation : java.util.List.of("PlacePersons", "GetAmount", "GetSharedCost", "GetJobStimuliForCalculation")) {
			wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + operation))
				.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
				.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
		}
	}

	@Test
	void createAndUpdatePostTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "Create?businessType=8&businessId=42")).willReturn(okJson("{\"calculationId\":7}")));
		wireMock.stubFor(post(urlEqualTo(CALCULATION + "Update?businessType=3&businessId=7")).willReturn(aResponse().withStatus(204)));

		assertThat(new String(client.create(8, 42, BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"calculationId\":7}");
		assertThat(client.update(3, 7, BODY.getBytes(UTF_8))).isEmpty();

		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "Create?businessType=8&businessId=42")).withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(CALCULATION + "Update?businessType=3&businessId=7")).withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void deleteIsADeleteWithAnEmptyBody() {
		wireMock.stubFor(delete(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7")).willReturn(aResponse().withStatus(200)));

		client.delete(3, 7, new byte[0]);

		wireMock.verify(1, deleteRequestedFor(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7"))
			.withHeader("Content-Length", equalTo("0"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8")));
	}

	@Test
	void deleteRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(delete(urlEqualTo(CALCULATION + "Delete?businessType=3&businessId=7"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beräkningen är slutlig\"}")));

		assertThatThrownBy(() -> client.delete(3, 7, new byte[0]))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beräkningen är slutlig");
	}
}
