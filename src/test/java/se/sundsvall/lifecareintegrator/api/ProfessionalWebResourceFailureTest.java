package se.sundsvall.lifecareintegrator.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebResourceFailureTest {

	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String PATH = "/{municipalityId}/professional-web/exchange";
	private static final String VALID_MUNICIPALITY_ID = "2281";

	@MockitoBean
	private ProfessionalWebExchange exchangeMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void exchangeWithInvalidMunicipalityId() {
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", INVALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "api2/Calculation/GetCalculation"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("exchange.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void pathOutsideApi2IsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "../WE.Flow.Html"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void unknownMethodIsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "PUT", "path", "api2/x"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	/**
	 * A traversal that starts with the required {@code api2}/{@code RenderPdf} prefix must be refused just like one
	 * that never carries the prefix at all ({@link #pathOutsideApi2IsRefused()}) — the whole point of the prefix check
	 * is defeated if {@code ..} segments are still allowed to walk back out of it.
	 */
	@Test
	void sameModuleTraversalImmediatelyAfterPrefixIsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "api2/../../IdentityPortalWeb/Login"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void shortSameModuleTraversalIsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "api2/../x"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void renderPdfSameModuleTraversalIsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "RenderPdf/../../x"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}

	@Test
	void deeperSameModuleTraversalIsRefused() {
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", VALID_MUNICIPALITY_ID)))
			.bodyValue(Map.of("method", "GET", "path", "api2/a/../../x"))
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(exchangeMock);
	}
}
