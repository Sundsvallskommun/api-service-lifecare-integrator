package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPaymentClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
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

class ProfessionalWebPaymentClientTest {

	private static final String API2 = "/WESE.FC.ProfessionalWeb/api2/";
	private static final String CREATE_PAYMENT = API2 + "Payment/Create?businessType=8&businessId=42";
	private static final String HEARTBEAT = "/WESE.FC.ProfessionalWeb/Heartbeat";
	private static final String LIFECARE_JSON = "{\"payment\":{\"amount\":1250.50},\"personnummer\":\"191212121212\"}";
	private static final String BODY = "{ \"amount\": 1250.50,\n \"text\": \"Utbetalning åäö\", \"unknown\": {\"a\": [1]} }";

	private WireMockServer wireMock;
	private ProfessionalWebPaymentClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebPaymentClient.class, wireMock.baseUrl());
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void paymentForCreateIsReadWithBusinessTypeThenBusinessId() {
		wireMock.stubFor(get(urlEqualTo(API2 + "Payment/GetPaymentForCreate?businessType=8&businessId=42")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getPaymentForCreate(8, 42), UTF_8)).isEqualTo(LIFECARE_JSON);

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

		assertThat(new String(client.createPayment(8, 42, BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"paymentId\":5}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void createPaymentFailingInLifecareIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.willReturn(aResponse().withStatus(500).withBody("{\"message\":\"Fel\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered with status 500: Fel");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentThatTimesOutIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payment/Create?businessType=8&businessId=42"))
			.willReturn(okJson("{}").withFixedDelay(7000)));

		assertThatThrownBy(() -> client.createPayment(8, 42, BODY.getBytes(UTF_8)))
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

		assertThat(new String(client.createPayment(8, 42, BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"paymentId\":5}");

		wireMock.verify(2, postRequestedFor(urlEqualTo(CREATE_PAYMENT)).withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void createPaymentRefusedByLifecareIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Beloppet saknas\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Beloppet saknas");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentAnsweredWithBadRequestIsNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(aResponse().withStatus(400).withBody("{\"Message\":\"Felaktig utbetalning\"}")));

		assertThatThrownBy(() -> client.createPayment(8, 42, BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(400, "Felaktig utbetalning");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPaymentAnsweredWithAnHtmlPageIsBadGatewayAndNotSentAgain() {
		wireMock.stubFor(post(urlEqualTo(CREATE_PAYMENT))
			.willReturn(ok("<html>Logga in</html>").withHeader("Content-Type", "text/html; charset=utf-8")));

		assertThatThrownBy(() -> client.createPayment(8, 42, BODY.getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(502, "Lifecare answered a write with an HTML page");

		wireMock.verify(1, anyRequestedFor(anyUrl()));
	}

	@Test
	void createPayeePostsTheBodyByteForByteWithoutATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(API2 + "Payee/Create")).willReturn(okJson("{\"payeeId\":3}")));

		assertThat(new String(client.createPayee(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"payeeId\":3}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(API2 + "Payee/Create"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
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
}
