package se.sundsvall.lifecareintegrator.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.api.model.DocumentMetadata;
import se.sundsvall.lifecareintegrator.api.model.PagedResponse;
import se.sundsvall.lifecareintegrator.service.FamilyCareService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class DocumentResourceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String DOCUMENT_ID = "12345";
	private static final String PATH = "/{municipalityId}/documents";

	@MockitoBean
	private FamilyCareService familyCareServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDocuments() {
		// Mock
		final var response = PagedResponse.<DocumentMetadata>create().withResults(List.of());
		when(familyCareServiceMock.getDocuments(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null)).thenReturn(response);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("partyId", PARTY_ID).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(new ParameterizedTypeReference<PagedResponse<DocumentMetadata>>() {})
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(response);
		verify(familyCareServiceMock).getDocuments(MUNICIPALITY_ID, PARTY_ID, null, null, null, null, null);
	}

	@Test
	void getDocumentContent() {
		// Mock
		final var content = "%PDF-1.4".getBytes();
		when(familyCareServiceMock.getDocumentContent(DOCUMENT_ID)).thenReturn(content);

		// Call
		final var result = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{documentId}/content").build(Map.of("municipalityId", MUNICIPALITY_ID, "documentId", DOCUMENT_ID)))
			.accept(APPLICATION_PDF)
			.exchange()
			.expectStatus().isOk()
			.expectBody(byte[].class)
			.returnResult()
			.getResponseBody();

		// Verification
		assertThat(result).isEqualTo(content);
		verify(familyCareServiceMock).getDocumentContent(DOCUMENT_ID);
	}
}
