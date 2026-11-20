package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebCalculationClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
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
}
