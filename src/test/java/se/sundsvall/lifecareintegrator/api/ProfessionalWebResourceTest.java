package se.sundsvall.lifecareintegrator.api;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.professionalweb.ProfessionalWebExchangeResponse;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebExchange;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebResourceTest {

	private static final String PATH = "/2281/professional-web/exchange";

	@MockitoBean
	private ProfessionalWebExchange exchangeMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void exchangeWithBody() {
		final var headers = HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (a, b) -> true);
		when(exchangeMock.exchange(eq("POST"), eq("api2/Decision/Create"), any(), any()))
			.thenReturn(new ProfessionalWebResponse(461, headers, "{\"m\":1}".getBytes(StandardCharsets.UTF_8), URI.create("https://x")));

		final var result = webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "POST", "path", "api2/Decision/Create", "params", Map.of("businessType", "8"), "body", Map.of("a", 1)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(ProfessionalWebExchangeResponse.class)
			.returnResult().getResponseBody();

		assertThat(result.status()).isEqualTo(461);
		assertThat(result.contentType()).isEqualTo("application/json");
		assertThat(new String(Base64.getDecoder().decode(result.body()), StandardCharsets.UTF_8)).isEqualTo("{\"m\":1}");
		final var params = new LinkedHashMap<String, String>();
		params.put("businessType", "8");
		verify(exchangeMock).exchange("POST", "api2/Decision/Create", params, "{\"a\":1}".getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void exchangeWithoutBody() {
		final var headers = HttpHeaders.of(Map.of(), (a, b) -> true);
		when(exchangeMock.exchange(eq("GET"), eq("RenderPdf/PrintDecision"), isNull(), isNull()))
			.thenReturn(new ProfessionalWebResponse(200, headers, new byte[] {
				'%', 'P'
			}, URI.create("https://x")));

		webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "GET", "path", "RenderPdf/PrintDecision"))
			.exchange()
			.expectStatus().isOk()
			.expectBody().jsonPath("$.body").isEqualTo("JVA=");
	}

	@Test
	void exchangeWithTheDocumentedExamplePathIsAccepted() {
		final var headers = HttpHeaders.of(Map.of("Content-Type", List.of("application/json")), (a, b) -> true);
		when(exchangeMock.exchange(eq("GET"), eq("api2/Calculation/GetCalculation"), any(), isNull()))
			.thenReturn(new ProfessionalWebResponse(200, headers, "{}".getBytes(StandardCharsets.UTF_8), URI.create("https://x")));

		webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "GET", "path", "api2/Calculation/GetCalculation"))
			.exchange()
			.expectStatus().isOk();
	}

	@Test
	void exchangeWithForm() {
		final var headers = HttpHeaders.of(Map.of("Content-Type", List.of("application/pdf")), (a, b) -> true);
		final var fields = List.of(Map.entry("51_0_2_1", "true"), Map.entry("51_0_2_1", "false"), Map.entry("X-LEGACY-TOKEN", "TOKEN"), Map.entry("empty", ""));
		final var params = new LinkedHashMap<String, String>();
		params.put("templateId", "t-1");
		params.put("decisionId", "134");
		when(exchangeMock.exchangeForm("RenderPdf/PrintDecision", params, fields))
			.thenReturn(new ProfessionalWebResponse(200, headers, "%PDF".getBytes(StandardCharsets.UTF_8), URI.create("https://x")));

		final var result = webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "POST", "path", "RenderPdf/PrintDecision", "params", params, "form", List.of(
				Map.of("name", "51_0_2_1", "value", "true"),
				Map.of("name", "51_0_2_1", "value", "false"),
				Map.of("name", "X-LEGACY-TOKEN", "value", "TOKEN"),
				Map.of("name", "empty", "value", ""))))
			.exchange()
			.expectStatus().isOk()
			.expectBody(ProfessionalWebExchangeResponse.class)
			.returnResult().getResponseBody();

		assertThat(result.status()).isEqualTo(200);
		assertThat(result.contentType()).isEqualTo("application/pdf");
		assertThat(new String(Base64.getDecoder().decode(result.body()), StandardCharsets.UTF_8)).isEqualTo("%PDF");
		verify(exchangeMock).exchangeForm("RenderPdf/PrintDecision", params, fields);
		verifyNoMoreInteractions(exchangeMock);
	}

	@Test
	void exchangeWithFormAndANullBodyIsAForm() {
		final var headers = HttpHeaders.of(Map.of(), (a, b) -> true);
		when(exchangeMock.exchangeForm(eq("RenderPdf/PrintDecision"), isNull(), any()))
			.thenReturn(new ProfessionalWebResponse(200, headers, new byte[0], URI.create("https://x")));

		webTestClient.post().uri(PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue("{\"method\":\"POST\",\"path\":\"RenderPdf/PrintDecision\",\"body\":null,\"form\":[{\"name\":\"a\",\"value\":\"b\"}]}")
			.exchange()
			.expectStatus().isOk();

		verify(exchangeMock).exchangeForm("RenderPdf/PrintDecision", null, List.of(Map.entry("a", "b")));
		verifyNoMoreInteractions(exchangeMock);
	}
}
