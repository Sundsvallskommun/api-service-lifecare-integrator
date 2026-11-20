package se.sundsvall.lifecareintegrator.api;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebDecisionService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebDecisionResourceTest {

	private static final String LIFECARE_JSON = "{\"decisionId\":9,\"personnummer\":\"191212121212\",\"unknown\":[1]}";
	private static final String BODY = "{ \"decisionId\": 9,\n \"text\": \"Beslut åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	@MockitoBean
	private ProfessionalWebDecisionService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDecisionProposal() {
		when(serviceMock.getDecisionProposal(42, "TotalSum", 7)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=7")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getDecisionProposal(42, "TotalSum", 7);
		verifyNoMoreInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"?calculationId=7", "?amountType=TotalSum", "?amountType=&calculationId=7", "?amountType=TotalSum&calculationId=", "?amountType=TotalSum&calculationId=abc",
		"?amountType=TotalSum&calculationId=0", "?amountType=TotalSum&calculationId=-1", ""
	})
	void getDecisionProposalWithoutAmountTypeAndCalculationIdIsRefused(final String query) {
		webTestClient.get().uri("/2281/professional-web/services/42/decisions/proposal" + query)
			.exchange()
			.expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void getDecisionReasons() {
		when(serviceMock.getDecisionReasons(5)).thenReturn("[]".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/decision-codes/5/reasons")).isEqualTo("[]");
		verify(serviceMock).getDecisionReasons(5);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDecision() {
		when(serviceMock.getDecision(9)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/decisions/9")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getDecision(9);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createDecisionAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createDecision(42, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/services/42/decisions")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).createDecision(42, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createDecisionWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.createDecision(42, "{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/services/42/decisions")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
	}

	@Test
	void updateDecisionAnswersOkWithLifecaresBody() {
		when(serviceMock.updateDecision(9, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		final var body = webTestClient.put().uri("/2281/professional-web/decisions/9")
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).updateDecision(9, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateDecisionWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.updateDecision(9, "{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.put().uri("/2281/professional-web/decisions/9")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
	}

	@Test
	void updateDecisionRefusedByLifecareIsUnprocessable() {
		when(serviceMock.updateDecision(9, "{}".getBytes(UTF_8))).thenThrow(Problem.valueOf(UNPROCESSABLE_CONTENT, "Beslutet är verkställt"));

		webTestClient.put().uri("/2281/professional-web/decisions/9")
			.contentType(APPLICATION_JSON)
			.bodyValue("{}")
			.exchange()
			.expectStatus().isEqualTo(422)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Beslutet är verkställt");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc", "99999999999999999999"
	})
	void idThatIsNotAPositiveNumberIsRefused(final String id) {
		webTestClient.get().uri("/2281/professional-web/services/" + id + "/decisions/proposal?amountType=TotalSum&calculationId=7").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/decision-codes/" + id + "/reasons").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/decisions/" + id).exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/2281/professional-web/services/" + id + "/decisions").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/decisions/" + id).contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		webTestClient.post().uri("/2281/professional-web/services/42/decisions").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/decisions/9").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad/professional-web/services/42/decisions/proposal?amountType=TotalSum&calculationId=7").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/decision-codes/5/reasons").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/decisions/9").exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/bad/professional-web/services/42/decisions").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/bad/professional-web/decisions/9").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	private String getJson(final String uri) {
		return webTestClient.get().uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}
}
