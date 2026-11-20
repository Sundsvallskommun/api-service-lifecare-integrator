package se.sundsvall.lifecareintegrator.api;

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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebPrintService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebPrintResourceTest {

	private static final byte[] PDF = "%PDF-1.4\nbinaryå".getBytes(UTF_8);

	@MockitoBean
	private ProfessionalWebPrintService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCalculationPdf() {
		when(serviceMock.getCalculationPdf(7)).thenReturn(PDF);

		assertThat(getPdf("/2281/professional-web/calculations/7/pdf")).isEqualTo(PDF);
		verify(serviceMock).getCalculationPdf(7);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDecisionPdf() {
		when(serviceMock.getDecisionPdf(9)).thenReturn(PDF);

		assertThat(getPdf("/2281/professional-web/decisions/9/pdf")).isEqualTo(PDF);
		verify(serviceMock).getDecisionPdf(9);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDocumentPdf() {
		when(serviceMock.getDocumentPdf(1)).thenReturn(PDF);

		assertThat(getPdf("/2281/professional-web/documents/1/pdf")).isEqualTo(PDF);
		verify(serviceMock).getDocumentPdf(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void printThatIsNotAPdfIsBadGateway() {
		when(serviceMock.getDecisionPdf(9)).thenThrow(Problem.valueOf(BAD_GATEWAY, "Lifecare answered the print with something that is not a PDF"));

		webTestClient.get().uri("/2281/professional-web/decisions/9/pdf").accept(APPLICATION_PDF)
			.exchange()
			.expectStatus().isEqualTo(502)
			.expectBody()
			.jsonPath("$.detail").isEqualTo("Lifecare answered the print with something that is not a PDF");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc", "99999999999999999999"
	})
	void idThatIsNotAPositiveNumberIsRefused(final String id) {
		webTestClient.get().uri("/2281/professional-web/calculations/" + id + "/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/decisions/" + id + "/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/documents/" + id + "/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad/professional-web/calculations/7/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/decisions/9/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/documents/1/pdf").accept(APPLICATION_PDF).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	private byte[] getPdf(final String uri) {
		return webTestClient.get().uri(uri)
			.accept(APPLICATION_PDF)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_PDF)
			.expectBody(byte[].class).returnResult().getResponseBody();
	}
}
