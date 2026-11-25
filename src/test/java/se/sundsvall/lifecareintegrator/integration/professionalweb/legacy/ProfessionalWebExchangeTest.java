package se.sundsvall.lifecareintegrator.integration.professionalweb.legacy;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.lifecareintegrator.integration.professionalweb.configuration.ProfessionalWebProperties;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSignIn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.http;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.properties;
import static se.sundsvall.lifecareintegrator.integration.professionalweb.session.SignInStubs.stubSignIn;

/**
 * Drives the pass-through against a fake Lifecare and identity provider: the session headers on a data call, both
 * session escalations, and refusals handed back untouched. Sign-in and the session itself are tested in the session
 * package.
 */
class ProfessionalWebExchangeTest {

	private static final String API = "/WESE.FC.ProfessionalWeb/api2/Thing/Get";
	private static final String PRINT = "/WESE.FC.ProfessionalWeb/RenderPdf/PrintDecision";
	private static final String FORM_BODY = "51_0_2_1=true&51_0_2_1=false&X-LEGACY-TOKEN=tok";
	private static final String PARAMETER_QUERY = """
		<form id="myForm" method="post"><input type="checkbox" name="51_0_2_1" value="true" checked><input type="hidden" name="51_0_2_1" value="false"><input type="hidden" name="X-LEGACY-TOKEN" value="tok"></form>
		""";

	private WireMockServer wireMock;
	private ProfessionalWebExchange exchange;
	private ProfessionalWebSession session;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		final var properties = properties(wireMock, "user", "sec ret&1", null);
		final var http = http();
		session = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());
		exchange = new ProfessionalWebExchange(properties, session, http);
		stubSignIn(wireMock);
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void signsInAndSendsTheSession() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(okJson("{\"id\":7}")));
		final var params = new LinkedHashMap<String, String>();
		params.put("id", "7");

		final var response = exchange.exchange("GET", "api2/Thing/Get", params, null);

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.bodyAsString()).isEqualTo("{\"id\":7}");
		assertThat(session.isEstablished()).isTrue();
		wireMock.verify(postRequestedFor(urlPathEqualTo("/idp/login/post"))
			.withRequestBody(containing("uid=user"))
			.withRequestBody(containing("otp=sec+ret%261")));
		wireMock.verify(getRequestedFor(urlPathEqualTo(API))
			.withQueryParam("id", equalTo("7"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok"))
			.withHeader("X-Requested-With", equalTo("XMLHttpRequest"))
			.withHeader("Cookie", containing("metadomain=Domain")));
	}

	@Test
	void bootstrapsTheModuleThenSignsInAgain() {
		wireMock.stubFor(post(urlPathEqualTo(API)).inScenario("s").whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(360)).willSetStateTo("still"));
		wireMock.stubFor(post(urlPathEqualTo(API)).inScenario("s").whenScenarioStateIs("still")
			.willReturn(aResponse().withStatus(302).withHeader("Location", "/IdentityPortalWeb/login")).willSetStateTo("fresh"));
		wireMock.stubFor(post(urlPathEqualTo(API)).inScenario("s").whenScenarioStateIs("fresh")
			.withRequestBody(equalToJson("{\"a\":1}")).willReturn(okJson("{\"saved\":1}")));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));

		final var response = exchange.exchange("POST", "/api2/Thing/Get", Map.of(), "{\"a\":1}".getBytes(StandardCharsets.UTF_8));

		assertThat(response.status()).isEqualTo(200);
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));
		wireMock.verify(2, postRequestedFor(urlPathEqualTo("/idp/login/post")));
	}

	@Test
	void refusalIsHandedBackAsItIs() {
		wireMock.stubFor(post(urlPathEqualTo(API)).willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Nej\"}")));

		final var response = exchange.exchange("POST", "api2/Thing/Get", null, null);

		assertThat(response.status()).isEqualTo(461);
		assertThat(response.bodyAsString()).contains("Nej");
	}

	@Test
	void deleteSendsItsBody() {
		wireMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.delete(urlPathEqualTo(API))
			.withRequestBody(equalToJson("{\"id\":5}")).willReturn(aResponse().withStatus(200)));

		assertThat(exchange.exchange("DELETE", "api2/Thing/Get", Map.of(), "{\"id\":5}".getBytes(StandardCharsets.UTF_8)).body()).isEmpty();
	}

	@Test
	void parameterQueryPageIsHandedBackWithoutEscalating() {
		wireMock.stubFor(get(urlPathEqualTo(PRINT)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/html; charset=utf-8").withBody(PARAMETER_QUERY)));

		final var response = exchange.exchange("GET", "RenderPdf/PrintDecision", printParams(), null);

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.bodyAsString()).contains("id=\"myForm\"");
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(PRINT)));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo("/idp/login/post")));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));
	}

	@Test
	void formIsPostedLikeABrowserDoes() {
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/pdf").withBody("%PDF-1.7")));
		final var fields = List.of(Map.entry("51_0_2_1", "true"), Map.entry("51_0_2_1", "false"), Map.entry("X-LEGACY-TOKEN", "tok"));

		final var response = exchange.exchangeForm("RenderPdf/PrintDecision", printParams(), fields);

		assertThat(response.status()).isEqualTo(200);
		assertThat(response.bodyAsString()).isEqualTo("%PDF-1.7");
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(PRINT))
			.withQueryParam("templateId", equalTo("t-1"))
			.withQueryParam("decisionId", equalTo("134"))
			.withQueryParam("hideRevisions", equalTo("true"))
			.withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
			.withHeader("Accept", containing("text/html"))
			.withHeader("Origin", equalTo(wireMock.baseUrl()))
			.withHeader("Referer", equalTo(wireMock.baseUrl() + PRINT + "?templateId=t-1&decisionId=134&hideRevisions=true"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok"))
			.withHeader("Cookie", containing("metadomain=Domain"))
			.withoutHeader("X-Requested-With")
			.withoutHeader("ajax-no-cross-domain-redirect")
			.withRequestBody(equalTo(FORM_BODY)));
	}

	@Test
	void formEscalatesLikeAnyOtherCall() {
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).inScenario("f").whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(360)).willSetStateTo("still"));
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).inScenario("f").whenScenarioStateIs("still")
			.willReturn(aResponse().withStatus(302).withHeader("Location", "/IdentityPortalWeb/login")).willSetStateTo("fresh"));
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).inScenario("f").whenScenarioStateIs("fresh")
			.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/pdf").withBody("%PDF")));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var fields = List.of(Map.entry("51_0_2_1", "true"), Map.entry("51_0_2_1", "false"), Map.entry("X-LEGACY-TOKEN", "tok"));

		final var response = exchange.exchangeForm("RenderPdf/PrintDecision", printParams(), fields);

		assertThat(response.status()).isEqualTo(200);
		wireMock.verify(3, postRequestedFor(urlPathEqualTo(PRINT))
			.withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
			.withRequestBody(equalTo(FORM_BODY)));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));
		wireMock.verify(2, postRequestedFor(urlPathEqualTo("/idp/login/post")));
	}

	@Test
	void formGivesUpWhenAFreshSessionIsRefused() {
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).willReturn(aResponse().withStatus(401)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var fields = List.of(Map.entry("a", "b"));

		assertThatThrownBy(() -> exchange.exchangeForm("RenderPdf/PrintDecision", Map.of(), fields))
			.hasMessageContaining("would not accept a freshly established session");
	}

	@Test
	void formRefusalIsHandedBackAsItIs() {
		wireMock.stubFor(post(urlPathEqualTo(PRINT)).willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Nej\"}")));

		final var response = exchange.exchangeForm("RenderPdf/PrintDecision", null, List.of(Map.entry("a", "")));

		assertThat(response.status()).isEqualTo(461);
		assertThat(response.bodyAsString()).contains("Nej");
		wireMock.verify(postRequestedFor(urlPathEqualTo(PRINT)).withRequestBody(equalTo("a=")));
	}

	@Test
	void formCannotLeaveTheModule() {
		final var fields = List.of(Map.entry("a", "b"));

		assertThatThrownBy(() -> exchange.exchangeForm("RenderPdf/../../IdentityPortalWeb/Login", Map.of(), fields))
			.hasMessageContaining("Refusing a ProfessionalWeb path that would leave");
		wireMock.verify(0, postRequestedFor(urlPathEqualTo("/IdentityPortalWeb/Login")));
	}

	@Test
	void givesUpWhenAFreshSessionIsRefused() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(aResponse().withStatus(401)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));

		assertThatThrownBy(() -> exchange.exchange("GET", "api2/Thing/Get", Map.of(), null))
			.hasMessageContaining("would not accept a freshly established session");
	}

	@Test
	void heartbeatThatFailsIsReported() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(aResponse().withStatus(360)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(aResponse().withStatus(500)));

		assertThatThrownBy(() -> exchange.exchange("GET", "api2/Thing/Get", Map.of(), null)).hasMessageContaining("would not start a session");
	}

	@Test
	void deadSeededSessionIsReported() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(aResponse().withStatus(360)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));

		assertThatThrownBy(() -> seededExchange("LEGACY-TOKEN=dead").exchange("GET", "api2/Thing/Get", Map.of(), null))
			.hasMessageContaining("would not accept a freshly established session");
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WE.Flow.Html")));
	}

	private static Map<String, String> printParams() {
		final var params = new LinkedHashMap<String, String>();
		params.put("templateId", "t-1");
		params.put("decisionId", "134");
		params.put("hideRevisions", "true");
		return params;
	}

	private ProfessionalWebExchange seededExchange(final String sessionCookie) {
		final ProfessionalWebProperties properties = properties(wireMock, null, null, sessionCookie);
		final var http = http();
		return new ProfessionalWebExchange(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), http);
	}
}
