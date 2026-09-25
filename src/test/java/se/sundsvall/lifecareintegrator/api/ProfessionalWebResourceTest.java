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
import static org.mockito.Mockito.verifyNoInteractions;
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
	void pathOutsideApi2IsRefused() {
		webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "GET", "path", "../WE.Flow.Html"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void unknownMethodIsRefused() {
		webTestClient.post().uri(PATH)
			.bodyValue(Map.of("method", "PUT", "path", "api2/x"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}
}
