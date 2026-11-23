package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDecisionClient;

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

class ProfessionalWebDecisionClientTest {

	private static final String DECISION = "/WESE.FC.ProfessionalWeb/api2/Decision/";
	private static final String LIFECARE_JSON = "{\"decisionId\":9,\"personnummer\":\"191212121212\",\"unknown\":[1]}";
	private static final String BODY = "{ \"decisionId\": 9,\n \"text\": \"Beslut åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	private WireMockServer wireMock;
	private ProfessionalWebDecisionClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebDecisionClient.class, wireMock.baseUrl());
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void proposalForServiceIsReadWithItsParametersInOrder() {
		final var url = DECISION + "GetProposalForService?businessType=8&businessId=42&amountType=TotalSum&calculationId=7&proposalId=0";
		wireMock.stubFor(get(urlEqualTo(url)).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getProposalForService(8, 42, "TotalSum", 7, 0), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(url)).withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void proposalForServiceWithoutACalculationSendsTheEmptyAmountType() {
		final var url = DECISION + "GetProposalForService?businessType=8&businessId=42&amountType=&calculationId=0&proposalId=0";
		wireMock.stubFor(get(urlEqualTo(url)).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getProposalForService(8, 42, "", 0, 0), UTF_8)).isEqualTo(LIFECARE_JSON);

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
		wireMock.stubFor(get(urlEqualTo(DECISION + "GetDecision?businessType=4&businessId=9")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getDecision(4, 9), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DECISION + "GetDecision?businessType=4&businessId=9")));
	}

	@Test
	void createPostsTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Create?businessType=8&businessId=42")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.create(8, 42, BODY.getBytes(UTF_8)), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, postRequestedFor(urlEqualTo(DECISION + "Create?businessType=8&businessId=42"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void updateIsAPostWithTheBodyByteForByte() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Update?businessType=4&businessId=9")).willReturn(aResponse().withStatus(204)));

		assertThat(client.update(4, 9, BODY.getBytes(UTF_8))).isEmpty();

		wireMock.verify(1, postRequestedFor(urlEqualTo(DECISION + "Update?businessType=4&businessId=9"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void updateRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(DECISION + "Update?businessType=4&businessId=9"))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beslutet är verkställt\"}")));

		assertThatThrownBy(() -> client.update(4, 9, "{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beslutet är verkställt");
	}
}
