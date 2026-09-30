package se.sundsvall.lifecareintegrator.integration.professionalweb;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Drives the pass-through against a fake Lifecare and identity provider: the SAML sign-in through MobilityGuard's
 * hidden fields, the session headers on a data call, both session escalations, and refusals handed back untouched.
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
		final var properties = properties("user", "sec ret&1");
		final var http = http(properties);
		session = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());
		exchange = new ProfessionalWebExchange(properties, session, http);
		stubSignIn();
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
	void unconfigured() {
		final var properties = new ProfessionalWebProperties(null, null, "a", "saml", null, null, null, Duration.ofMinutes(1), 1, 1, null, Duration.ofMinutes(5));
		final var http = http(properties);
		final var unconfigured = new ProfessionalWebExchange(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), http);

		assertThatThrownBy(() -> unconfigured.exchange("GET", "api2/x", Map.of(), null)).hasMessageContaining("not configured");
	}

	@Test
	void sessionPastItsTtlIsReplaced() {
		final var properties = properties("user", "pw");
		final var http = http(properties);
		final var clock = new MutableClock(Instant.parse("2026-09-25T08:00:00Z"));
		final var timed = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, clock);

		timed.prepare();
		clock.now = clock.now.plus(Duration.ofMinutes(20));
		timed.prepare();

		wireMock.verify(2, postRequestedFor(urlPathEqualTo("/idp/login/post")));
	}

	@Test
	void wrongCredentials() {
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).atPriority(1).willReturn(ok(loginPage("/idp/login/retry"))));

		assertThatThrownBy(() -> exchange.exchange("GET", "api2/Thing/Get", Map.of(), null)).hasMessageContaining("sign in a second time");
	}

	@Test
	void waitingRoom() {
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).atPriority(1)
			.willReturn(ok("<form action=\"/idp/wait\"><input type=hidden name=poll value=1></form>")));
		wireMock.stubFor(post(urlPathEqualTo("/idp/wait")).willReturn(ok("<form action=\"/idp/wait\"><input type=hidden name=poll value=2></form>")));

		assertThatThrownBy(() -> exchange.exchange("GET", "api2/Thing/Get", Map.of(), null)).hasMessageContaining("waiting at /idp/wait");
	}

	@Test
	void noPasswordField() {
		wireMock.stubFor(get(urlPathEqualTo("/idp/login")).atPriority(1).willReturn(ok("<p>Ange engångskod</p>")));

		assertThatThrownBy(() -> exchange.exchange("GET", "api2/Thing/Get", Map.of(), null)).hasMessageContaining("no password field");
	}

	@Test
	void noAccount() {
		final var properties = properties(null, null);
		final var signIn = new ProfessionalWebSignIn(properties, http(properties));

		assertThatThrownBy(() -> signIn.signIn(new ProfessionalWebCookies(Clock.systemUTC()))).hasMessageContaining("No Lifecare account configured");
	}

	@Test
	void unreachable() {
		final var properties = new ProfessionalWebProperties("http://localhost:1", "d", "a", "saml", null, "u", "p", Duration.ofMinutes(1), 1, 1, null, Duration.ofMinutes(5));
		final var http = http(properties);
		final var broken = new ProfessionalWebExchange(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), http);

		assertThatThrownBy(() -> broken.exchange("GET", "api2/x", Map.of(), null)).hasMessageContaining("could not be reached");
	}

	private static Map<String, String> printParams() {
		final var params = new LinkedHashMap<String, String>();
		params.put("templateId", "t-1");
		params.put("decisionId", "134");
		params.put("hideRevisions", "true");
		return params;
	}

	private ProfessionalWebHttp http(final ProfessionalWebProperties properties) {
		return new ProfessionalWebHttp(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build(), properties);
	}

	private ProfessionalWebProperties properties(final String username, final String password) {
		return seeded(username, password, null);
	}

	private ProfessionalWebProperties seeded(final String username, final String password, final String sessionCookie) {
		return new ProfessionalWebProperties(wireMock.baseUrl() + "/", "Domain", "Actor_Professional", "saml", "Sundsvall_Intra",
			username, password, Duration.ofMinutes(20), 1, 5, sessionCookie, Duration.ofMinutes(5));
	}

	private ProfessionalWebExchange seededExchange(final String sessionCookie) {
		final var properties = seeded(null, null, sessionCookie);
		final var http = http(properties);
		return new ProfessionalWebExchange(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), http);
	}

	@Test
	void seededSessionIsUsedWithoutSigningIn() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(okJson("{\"ok\":1}")));

		final var response = seededExchange("ASP.NET_SessionId=s1; LEGACY-TOKEN=seeded").exchange("GET", "api2/Thing/Get", Map.of(), null);

		assertThat(response.status()).isEqualTo(200);
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WE.Flow.Html")));
		wireMock.verify(getRequestedFor(urlPathEqualTo(API))
			.withHeader("X-LEGACY-TOKEN", equalTo("seeded"))
			.withHeader("Cookie", containing("ASP.NET_SessionId=s1")));
	}

	@Test
	void seededSessionWithoutLegacyToken() {
		final var seeded = seededExchange("ASP.NET_SessionId=s1");

		assertThatThrownBy(() -> seeded.exchange("GET", "api2/Thing/Get", Map.of(), null)).hasMessageContaining("not a signed-in session");
	}

	@Test
	void deadSeededSessionIsReported() {
		wireMock.stubFor(get(urlPathEqualTo(API)).willReturn(aResponse().withStatus(360)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));

		assertThatThrownBy(() -> seededExchange("LEGACY-TOKEN=dead").exchange("GET", "api2/Thing/Get", Map.of(), null))
			.hasMessageContaining("would not accept a freshly established session");
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WE.Flow.Html")));
	}

	@Test
	void keepAliveOnlyWithASession() {
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var properties = seeded(null, null, "LEGACY-TOKEN=seeded");
		final var http = http(properties);
		final var seededSession = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());

		seededSession.keepAlive();
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));

		seededSession.prepare();
		seededSession.keepAlive();
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat"))
			.withHeader("Cookie", containing("LEGACY-TOKEN=seeded")));
	}

	@Test
	void keepAliveResetsAnExpiredSessionInsteadOfPingingIt() {
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")).willReturn(ok()));
		final var properties = seeded(null, null, "LEGACY-TOKEN=seeded");
		final var http = http(properties);
		final var clock = new MutableClock(Instant.parse("2026-09-25T08:00:00Z"));
		final var seededSession = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, clock);

		seededSession.prepare();
		clock.now = clock.now.plus(Duration.ofMinutes(20));

		seededSession.keepAlive();

		// A session already past its TTL is thrown away rather than pinged with cookies this service itself would
		// refuse for a real call.
		wireMock.verify(0, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/Heartbeat")));
		assertThat(seededSession.isEstablished()).isFalse();
	}

	@Test
	void keepAliveFailureIsSwallowed() {
		final var properties = new ProfessionalWebProperties("http://localhost:1", "d", "a", "saml", null, null, null, Duration.ofMinutes(1), 1, 1,
			"LEGACY-TOKEN=x", Duration.ofMinutes(5));
		final var http = http(properties);
		final var seededSession = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());
		seededSession.prepare();

		assertThatNoException().isThrownBy(seededSession::keepAlive);
	}

	private void stubSignIn() {
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html")).withQueryParam("domain", equalTo("Domain"))
			.willReturn(aResponse().withStatus(302).withHeader("Location", "/idp/login").withHeader("Set-Cookie", "ASP.NET_SessionId=s; path=/")));
		wireMock.stubFor(get(urlPathEqualTo("/idp/login")).willReturn(ok(loginPage("/idp/login/post"))));
		wireMock.stubFor(post(urlPathEqualTo("/idp/login/post")).willReturn(ok("""
			<form method="post" action="/IdentityPortalWeb/acs"><textarea name="SAMLResponse">PHNhbWw+</textarea></form>
			""")));
		wireMock.stubFor(post(urlPathEqualTo("/IdentityPortalWeb/acs")).willReturn(aResponse().withStatus(302)
			.withHeader("Location", "/WE.Flow.Html/").withHeader("Set-Cookie", "LEGACY-TOKEN=tok; path=/; secure")));
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html/")).willReturn(ok("<html>Lifecare</html>")));
	}

	private static String loginPage(final String action) {
		return """
			<form action="%s" method="post">
			  <input type="hidden" name="uid" value="">
			  <input type="hidden" name="otp" value="">
			  <input type="hidden" name="state" value="s1">
			</form>
			""".formatted(action);
	}

	private static final class MutableClock extends Clock {

		private Instant now;

		private MutableClock(final Instant now) {
			this.now = now;
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(final ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
