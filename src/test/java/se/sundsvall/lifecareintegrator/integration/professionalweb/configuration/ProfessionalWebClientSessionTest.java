package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import feign.Feign;
import feign.okhttp.OkHttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebClient;
import se.sundsvall.lifecareintegrator.integration.professionalweb.legacy.ProfessionalWebExchange;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebHttp;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSession;
import se.sundsvall.lifecareintegrator.integration.professionalweb.session.ProfessionalWebSignIn;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
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
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

class ProfessionalWebClientSessionTest {

	private static final String LIST_REMINDERS = "/WESE.FC.ProfessionalWeb/api2/Reminders/ListRemindersByServiceId";
	private static final String HEARTBEAT = "/WESE.FC.ProfessionalWeb/Heartbeat";
	private static final String SIGN_IN = "/idp/login/post";
	private static final String LIFECARE_JSON = "{\"id\":42,\"unknownField\":{\"nested\":[1,2]},\"personnummer\":\"191212121212\"}";

	private WireMockServer wireMock;
	private ProfessionalWebExchange exchange;
	private ProfessionalWebClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		final var properties = properties(wireMock.baseUrl() + "/");
		final var http = http(properties);
		final var session = new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC());
		exchange = new ProfessionalWebExchange(properties, session, http);
		client = client(properties, session, wireMock.baseUrl());
		stubSignIn();
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void readGoesThroughTheSessionAndReturnsLifecareJsonUntouched() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(okJson(LIFECARE_JSON)));

		final var body = client.listRemindersByServiceId(42);

		assertThat(new String(body, UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(1, getRequestedFor(urlEqualTo(LIST_REMINDERS + "?id=42"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok"))
			.withHeader("Cookie", containing("LEGACY-TOKEN=tok"))
			.withHeader("Cookie", containing("metadomain=Domain"))
			.withHeader("X-Requested-With", equalTo("XMLHttpRequest"))
			.withHeader("ajax-no-cross-domain-redirect", equalTo("true")));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void sessionRequiredStatusBootstrapsTheModuleAndAsksAgain() {
		stubFirstAnswerThenJson(aResponse().withStatus(360));

		assertThat(new String(client.listRemindersByServiceId(42), UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
		wireMock.verify(2, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void unauthorizedBootstrapsTheModuleAndAsksAgain() {
		stubFirstAnswerThenJson(aResponse().withStatus(401));

		assertThat(new String(client.listRemindersByServiceId(42), UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void redirectToTheIdentityPortalBootstrapsTheModuleAndAsksAgain() {
		stubFirstAnswerThenJson(aResponse().withStatus(302).withHeader("Location", "/IdentityPortalWeb/login"));

		assertThat(new String(client.listRemindersByServiceId(42), UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void loginPageAsHtmlBootstrapsTheModuleAndAsksAgain() {
		stubFirstAnswerThenJson(aResponse().withStatus(200).withHeader("Content-Type", "text/html; charset=utf-8").withBody("<html>Logga in</html>"));

		assertThat(new String(client.listRemindersByServiceId(42), UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void stillRefusedAfterBootstrapSignsInAgainAndAsksOnceMore() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("s").whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(360)).willSetStateTo("still"));
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("s").whenScenarioStateIs("still")
			.willReturn(aResponse().withStatus(360)).willSetStateTo("fresh"));
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("s").whenScenarioStateIs("fresh")
			.willReturn(okJson(LIFECARE_JSON)));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));

		assertThat(new String(client.listRemindersByServiceId(42), UTF_8)).isEqualTo(LIFECARE_JSON);
		wireMock.verify(3, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
		wireMock.verify(2, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void refusedEvenAfterAFreshSignInIsBadGateway() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(aResponse().withStatus(360)));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));

		assertThatThrownBy(() -> client.listRemindersByServiceId(42))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, "Lifecare would not accept a freshly established session (status 360)");
		wireMock.verify(3, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)));
		wireMock.verify(2, postRequestedFor(urlPathEqualTo(SIGN_IN)));
	}

	@Test
	void rotatedCookiesAreTakenIntoTheSession() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("r").whenScenarioStateIs(STARTED)
			.willReturn(okJson("{}").withHeader("Set-Cookie", "LEGACY-TOKEN=rotated; path=/")).willSetStateTo("rotated"));
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("r").whenScenarioStateIs("rotated")
			.willReturn(okJson("{}")));

		client.listRemindersByServiceId(1);
		client.listRemindersByServiceId(2);

		wireMock.verify(1, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)).withQueryParam("id", equalTo("1"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)).withQueryParam("id", equalTo("2"))
			.withHeader("X-LEGACY-TOKEN", equalTo("rotated"))
			.withHeader("Cookie", containing("LEGACY-TOKEN=rotated")));
	}

	@Test
	void refusalIsAProblemWithTheMappedStatus() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Nej\"}")));

		assertThatThrownBy(() -> client.listRemindersByServiceId(42))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Nej");
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)));
	}

	@Test
	void htmlErrorPageIsARefusalNotALostSession() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS))
			.willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/html").withBody("<html>Server Error</html>")));

		assertThatThrownBy(() -> client.listRemindersByServiceId(42))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(ThrowableProblem::getStatus, ThrowableProblem::getDetail)
			.containsExactly(BAD_GATEWAY, "Lifecare answered with status 500");
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(LIST_REMINDERS)));
		wireMock.verify(0, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void unreachableLifecareIsBadGateway() {
		final var properties = properties("http://localhost:1", "ASP.NET_SessionId=s; LEGACY-TOKEN=tok");
		final var http = http(properties);
		final var unreachable = client(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), "http://localhost:1");

		assertThatThrownBy(() -> unreachable.listRemindersByServiceId(42))
			.asInstanceOf(type(ThrowableProblem.class))
			.hasFieldOrPropertyWithValue("status", BAD_GATEWAY)
			.extracting(ThrowableProblem::getDetail, STRING)
			.startsWith("Lifecare could not be reached");
	}

	@Test
	void unconfiguredProfessionalWebIsBadGatewayWithoutAnyCall() {
		final var properties = properties(null);
		final var http = http(properties);
		final var unconfigured = client(properties, new ProfessionalWebSession(properties, new ProfessionalWebSignIn(properties, http), http, Clock.systemUTC()), wireMock.baseUrl());

		assertThatThrownBy(() -> unconfigured.listRemindersByServiceId(42))
			.asInstanceOf(type(ThrowableProblem.class))
			.hasFieldOrPropertyWithValue("status", BAD_GATEWAY)
			.extracting(ThrowableProblem::getDetail, STRING)
			.contains("not configured");
		assertThat(wireMock.getAllServeEvents()).isEmpty();
	}

	@Test
	void readLogsMethodPathAndStatusButNeverTheBody() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(aResponse().withStatus(404).withBody("{\"secret\":\"191212121212\"}")));

		final var events = captureLogs(() -> assertThatThrownBy(() -> client.listRemindersByServiceId(42))
			.isInstanceOfSatisfying(ThrowableProblem.class, problem -> assertThat(problem.getStatus().value()).isEqualTo(404)));

		assertThat(events).extracting(ILoggingEvent::getFormattedMessage)
			.contains("Lifecare answered GET /WESE.FC.ProfessionalWeb/api2/Reminders/ListRemindersByServiceId with status 404")
			.noneMatch(message -> message.contains("191212121212"));
	}

	@Test
	void successfulReadLogsNoBody() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(okJson(LIFECARE_JSON)));

		final var events = captureLogs(() -> client.listRemindersByServiceId(42));

		assertThat(events).extracting(ILoggingEvent::getFormattedMessage)
			.noneMatch(message -> message.contains("191212121212") || message.contains("unknownField"));
	}

	@Test
	void resourceReadThenExchangeShareOneSession() {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).willReturn(okJson(LIFECARE_JSON)));
		wireMock.stubFor(get(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Thing/Get")).willReturn(okJson("{}")));

		client.listRemindersByServiceId(42);
		final var response = exchange.exchange("GET", "api2/Thing/Get", Map.of(), null);

		assertThat(response.status()).isEqualTo(200);
		wireMock.verify(1, postRequestedFor(urlPathEqualTo(SIGN_IN)));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Thing/Get"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	private void stubFirstAnswerThenJson(final ResponseDefinitionBuilder first) {
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("b").whenScenarioStateIs(STARTED)
			.willReturn(first).willSetStateTo("bootstrapped"));
		wireMock.stubFor(get(urlPathEqualTo(LIST_REMINDERS)).inScenario("b").whenScenarioStateIs("bootstrapped")
			.willReturn(okJson(LIFECARE_JSON)));
		wireMock.stubFor(get(urlPathEqualTo(HEARTBEAT)).willReturn(ok()));
	}

	private static List<ILoggingEvent> captureLogs(final Runnable action) {
		final var logger = (Logger) LoggerFactory.getLogger("se.sundsvall.lifecareintegrator.integration.professionalweb");
		final var originalLevel = logger.getLevel();
		final var appender = new ListAppender<ILoggingEvent>();
		appender.start();
		logger.addAppender(appender);
		logger.setLevel(Level.TRACE);
		try {
			action.run();
			return List.copyOf(appender.list);
		} finally {
			logger.setLevel(originalLevel);
			logger.detachAppender(appender);
		}
	}

	private static ProfessionalWebHttp http(final ProfessionalWebProperties properties) {
		return new ProfessionalWebHttp(new okhttp3.OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).build());
	}

	private static ProfessionalWebProperties properties(final String url) {
		return properties(url, null);
	}

	private static ProfessionalWebProperties properties(final String url, final String sessionCookie) {
		return new ProfessionalWebProperties(url, "Domain", "Actor_Professional", "saml", "Sundsvall_Intra",
			"user", "pw", Duration.ofMinutes(20), 1, 5, sessionCookie, Duration.ofMinutes(5));
	}

	private static ProfessionalWebClient client(final ProfessionalWebProperties properties, final ProfessionalWebSession session, final String target) {
		final var builder = Feign.builder()
			.contract(new SpringMvcContract())
			.client(new OkHttpClient());
		new ProfessionalWebApi2Configuration().feignBuilderCustomizer(properties, session).customize(builder);
		return builder.target(ProfessionalWebClient.class, target);
	}

	private void stubSignIn() {
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html")).withQueryParam("domain", equalTo("Domain"))
			.willReturn(aResponse().withStatus(302).withHeader("Location", "/idp/login").withHeader("Set-Cookie", "ASP.NET_SessionId=s; path=/")));
		wireMock.stubFor(get(urlPathEqualTo("/idp/login")).willReturn(ok("""
			<form action="/idp/login/post" method="post">
			  <input type="hidden" name="uid" value="">
			  <input type="hidden" name="otp" value="">
			</form>
			""")));
		wireMock.stubFor(post(urlPathEqualTo(SIGN_IN)).willReturn(ok("""
			<form method="post" action="/IdentityPortalWeb/acs"><textarea name="SAMLResponse">PHNhbWw+</textarea></form>
			""")));
		wireMock.stubFor(post(urlPathEqualTo("/IdentityPortalWeb/acs")).willReturn(aResponse().withStatus(302)
			.withHeader("Location", "/WE.Flow.Html/").withHeader("Set-Cookie", "LEGACY-TOKEN=tok; path=/; secure")));
		wireMock.stubFor(get(urlPathEqualTo("/WE.Flow.Html/")).willReturn(ok("<html>Lifecare</html>")));
	}
}
