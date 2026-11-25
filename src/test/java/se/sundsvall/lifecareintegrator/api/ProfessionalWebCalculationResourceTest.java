package se.sundsvall.lifecareintegrator.api;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebCalculationResourceTest {

	private static final String LIFECARE_JSON = "{\"calculationId\":7,\"personnummer\":\"191212121212\",\"rows\":[{\"amount\":12.50}]}";
	private static final String BODY = "{ \"calculationId\": 7,\n \"text\": \"Beräkning åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	@MockitoBean
	private ProfessionalWebService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCalculationProposal() {
		when(serviceMock.getCalculationProposal(42)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/calculations/proposal")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getCalculationProposal(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getLatestCalculation() {
		when(serviceMock.getLatestCalculation(42)).thenReturn("[]".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/calculations/latest")).isEqualTo("[]");
		verify(serviceMock).getLatestCalculation(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getCalculation() {
		when(serviceMock.getCalculation(7)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/calculations/7")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getCalculation(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getCalculationForEdit() {
		when(serviceMock.getCalculationForEdit(7)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/calculations/7/edit")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getCalculationForEdit(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void placePersonsAnswersOkWithLifecaresAnswer() {
		when(serviceMock.placePersons(BODY.getBytes(UTF_8))).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(postOk("/2281/professional-web/calculations/place-persons")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).placePersons(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getCalculationAmountAnswersOkWithLifecaresAnswer() {
		when(serviceMock.getCalculationAmount(BODY.getBytes(UTF_8))).thenReturn("{\"amount\":12.50}".getBytes(UTF_8));

		assertThat(postOk("/2281/professional-web/calculations/amount")).isEqualTo("{\"amount\":12.50}");
		verify(serviceMock).getCalculationAmount(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getCalculationSharedCostAnswersOkWithLifecaresBareNumber() {
		when(serviceMock.getCalculationSharedCost(BODY.getBytes(UTF_8))).thenReturn("1250.50".getBytes(UTF_8));

		assertThat(postOk("/2281/professional-web/calculations/shared-cost")).isEqualTo("1250.50");
		verify(serviceMock).getCalculationSharedCost(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getCalculationJobStimuliAnswersOkWithLifecaresAnswer() {
		when(serviceMock.getCalculationJobStimuli(BODY.getBytes(UTF_8))).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(postOk("/2281/professional-web/calculations/job-stimuli")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getCalculationJobStimuli(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createCalculationAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createCalculation(42, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		final var body = webTestClient.post().uri("/2281/professional-web/services/42/calculations")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).createCalculation(42, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createCalculationWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.createCalculation(42, "{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/services/42/calculations").contentType(APPLICATION_JSON).bodyValue("{}").exchange()
			.expectStatus().isNoContent().expectBody().isEmpty();

		verify(serviceMock).createCalculation(42, "{}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateCalculationAnswersOkWithLifecaresBody() {
		when(serviceMock.updateCalculation(7, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		final var body = webTestClient.put().uri("/2281/professional-web/calculations/7")
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();

		assertThat(body).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).updateCalculation(7, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateCalculationWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.updateCalculation(7, "{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.put().uri("/2281/professional-web/calculations/7").contentType(APPLICATION_JSON).bodyValue("{}").exchange()
			.expectStatus().isNoContent().expectBody().isEmpty();

		verify(serviceMock).updateCalculation(7, "{}".getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void deleteCalculationIsNoContent() {
		webTestClient.delete().uri("/2281/professional-web/calculations/7")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(serviceMock).deleteCalculation(7);
		verifyNoMoreInteractions(serviceMock);
	}

	private String getJson(final String uri) {
		return webTestClient.get().uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}

	private String postOk(final String uri) {
		return webTestClient.post().uri(uri)
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}
}
