package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebPrintClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
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

class ProfessionalWebPrintClientTest {

	private static final String PRINT_DECISION = "/WESE.FC.ProfessionalWeb/RenderPdf/PrintDecision";
	private static final String QUERY = "?templateId=885bfb68-c97b-47c0-921d-ef00caaa2423&decisionId=9&hideRevisions=true";
	private static final String HEARTBEAT = "/WESE.FC.ProfessionalWeb/Heartbeat";
	private static final byte[] PDF = "%PDF-1.4\n%âãÏÓ\nbinary".getBytes(UTF_8);
	private static final String PARAMETER_PAGE = """
		<html><body><form id="myForm" method="post">
		  <input type="hidden" name="X-LEGACY-TOKEN" value="page-token">
		  <input type="checkbox" name="51_0_2_1" value="true" checked>
		  <input type="hidden" name="51_0_2_1" value="false">
		</form></body></html>
		""";

	private WireMockServer wireMock;
	private ProfessionalWebPrintClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebPrintClient.class, wireMock.baseUrl());
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void renderAsksForThePrintWithTheQueryInOrderAndReturnsThePdfBytes() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));

		assertThat(client.render("PrintDecision", decisionQuery())).isEqualTo(PDF);

		wireMock.verify(1, getRequestedFor(urlEqualTo(PRINT_DECISION + QUERY))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void parameterQuestionsAreHandedBackNotTakenForALostSession() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok(PARAMETER_PAGE).withHeader("Content-Type", "text/html; charset=utf-8")));

		assertThat(new String(client.render("PrintDecision", decisionQuery()), UTF_8)).isEqualTo(PARAMETER_PAGE);

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

		assertThat(client.render("PrintDecision", decisionQuery())).isEqualTo(PDF);

		wireMock.verify(2, getRequestedFor(urlPathEqualTo(PRINT_DECISION)));
		wireMock.verify(1, getRequestedFor(urlPathEqualTo(HEARTBEAT)));
	}

	@Test
	void submitParametersPostsTheFormLikeABrowserToTheSamePathAndQuery() {
		final var form = "X-LEGACY-TOKEN=page-token&51_0_2_1=true&51_0_2_1=false";
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(ok().withHeader("Content-Type", "application/pdf").withBody(PDF)));

		assertThat(client.submitParameters("PrintDecision", decisionQuery(), form.getBytes(UTF_8))).isEqualTo(PDF);

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
	void submitParametersRefusedByLifecareIsTheSameProblemAsOtherRefusals() {
		wireMock.stubFor(post(urlEqualTo(PRINT_DECISION + QUERY))
			.willReturn(aResponse().withStatus(461).withBody("{\"exceptionMessage\":\"Mallen saknas\"}")));

		assertThatThrownBy(() -> client.submitParameters("PrintDecision", decisionQuery(), "a=b".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Mallen saknas");
	}

	@Test
	void renderRefusedByLifecareIsAProblem() {
		wireMock.stubFor(get(urlEqualTo(PRINT_DECISION + QUERY)).willReturn(aResponse().withStatus(404)));

		assertThatThrownBy(() -> client.render("PrintDecision", decisionQuery()))
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
