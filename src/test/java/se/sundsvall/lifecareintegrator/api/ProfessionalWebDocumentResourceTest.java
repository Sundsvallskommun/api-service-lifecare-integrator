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
import se.sundsvall.lifecareintegrator.service.ProfessionalWebDocumentService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ProfessionalWebDocumentResourceTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String LIFECARE_JSON = "{\"documentId\":1,\"content\":\"Anteckning åäö\",\"personnummer\":\"191212121212\"}";
	private static final String BODY = "{\"documentId\": 1,\n \"content\": \"Anteckning åäö\", \"unknown\": {\"a\": [1, 2.50]}}";

	@MockitoBean
	private ProfessionalWebDocumentService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDocuments() {
		when(serviceMock.getDocuments("2281", PARTY_ID)).thenReturn("[]".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/persons/" + PARTY_ID + "/documents")).isEqualTo("[]");
		verify(serviceMock).getDocuments("2281", PARTY_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDocumentsForAPartyIdPartyDoesNotKnowIsNotFound() {
		when(serviceMock.getDocuments("2281", PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'"));

		webTestClient.get().uri("/2281/professional-web/persons/" + PARTY_ID + "/documents")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("No person number found for partyId '" + PARTY_ID + "'");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"not-a-uuid", "12345"
	})
	void getDocumentsWithAPartyIdThatIsNotAUuidIsRefused(final String partyId) {
		webTestClient.get().uri("/2281/professional-web/persons/" + partyId + "/documents").exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void getJournalNote() {
		when(serviceMock.getJournalNote(1)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/journal-notes/1")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getJournalNote(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getJournalNoteForEdit() {
		when(serviceMock.getJournalNoteForEdit(1)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/journal-notes/1/edit")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getJournalNoteForEdit(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDocument() {
		when(serviceMock.getDocument(1)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/documents/1")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getDocument(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getDocumentForEdit() {
		when(serviceMock.getDocumentForEdit(1)).thenReturn(LIFECARE_JSON.getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/documents/1/edit")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).getDocumentForEdit(1);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void getProposals() {
		when(serviceMock.getJournalNoteProposal(42)).thenReturn("{\"note\":1}".getBytes(UTF_8));
		when(serviceMock.getDocumentProposal(42)).thenReturn("{\"document\":1}".getBytes(UTF_8));

		assertThat(getJson("/2281/professional-web/services/42/journal-notes/proposal")).isEqualTo("{\"note\":1}");
		assertThat(getJson("/2281/professional-web/services/42/documents/proposal")).isEqualTo("{\"document\":1}");
		verify(serviceMock).getJournalNoteProposal(42);
		verify(serviceMock).getDocumentProposal(42);
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createJournalNoteAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createJournalNote(BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"documentId\":1}".getBytes(UTF_8)));

		assertThat(post("/2281/professional-web/journal-notes")).isEqualTo("{\"documentId\":1}");
		verify(serviceMock).createJournalNote(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createDocumentAnswersCreatedWithLifecaresBody() {
		when(serviceMock.createDocument(BODY.getBytes(UTF_8))).thenReturn(Optional.of("{\"documentId\":2}".getBytes(UTF_8)));

		assertThat(post("/2281/professional-web/documents")).isEqualTo("{\"documentId\":2}");
		verify(serviceMock).createDocument(BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void createWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.createJournalNote("{}".getBytes(UTF_8))).thenReturn(Optional.empty());
		when(serviceMock.createDocument("{}".getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.post().uri("/2281/professional-web/journal-notes").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isNoContent();
		webTestClient.post().uri("/2281/professional-web/documents").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isNoContent();
	}

	@Test
	void updateJournalNoteAnswersOkWithLifecaresBody() {
		when(serviceMock.updateJournalNote(1, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		assertThat(put("/2281/professional-web/journal-notes/1")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).updateJournalNote(1, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateDocumentAnswersOkWithLifecaresBody() {
		when(serviceMock.updateDocument(1, BODY.getBytes(UTF_8))).thenReturn(Optional.of(LIFECARE_JSON.getBytes(UTF_8)));

		assertThat(put("/2281/professional-web/documents/1")).isEqualTo(LIFECARE_JSON);
		verify(serviceMock).updateDocument(1, BODY.getBytes(UTF_8));
		verifyNoMoreInteractions(serviceMock);
	}

	@Test
	void updateWithoutLifecareAnswerIsNoContent() {
		when(serviceMock.updateJournalNote(1, BODY.getBytes(UTF_8))).thenReturn(Optional.empty());
		when(serviceMock.updateDocument(1, BODY.getBytes(UTF_8))).thenReturn(Optional.empty());

		webTestClient.put().uri("/2281/professional-web/journal-notes/1").contentType(APPLICATION_JSON).bodyValue(BODY.getBytes(UTF_8)).exchange()
			.expectStatus().isNoContent().expectBody().isEmpty();
		webTestClient.put().uri("/2281/professional-web/documents/1").contentType(APPLICATION_JSON).bodyValue(BODY.getBytes(UTF_8)).exchange()
			.expectStatus().isNoContent().expectBody().isEmpty();
	}

	@Test
	void updateWithAnotherDocumentIdIsRefused() {
		when(serviceMock.updateJournalNote(1, "{\"documentId\": 2}".getBytes(UTF_8))).thenThrow(Problem.valueOf(BAD_REQUEST, "The body's documentId must be 1"));

		webTestClient.put().uri("/2281/professional-web/journal-notes/1")
			.contentType(APPLICATION_JSON)
			.bodyValue("{\"documentId\": 2}")
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("The body's documentId must be 1");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1", "abc", "99999999999999999999"
	})
	void idThatIsNotAPositiveNumberIsRefused(final String id) {
		webTestClient.get().uri("/2281/professional-web/journal-notes/" + id).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/journal-notes/" + id + "/edit").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/documents/" + id).exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/documents/" + id + "/edit").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/services/" + id + "/journal-notes/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/2281/professional-web/services/" + id + "/documents/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/journal-notes/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/documents/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		webTestClient.post().uri("/2281/professional-web/journal-notes").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/2281/professional-web/documents").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/journal-notes/1").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/2281/professional-web/documents/1").contentType(APPLICATION_JSON).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		webTestClient.get().uri("/bad/professional-web/persons/" + PARTY_ID + "/documents").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/journal-notes/1").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/documents/1/edit").exchange().expectStatus().isBadRequest();
		webTestClient.get().uri("/bad/professional-web/services/42/documents/proposal").exchange().expectStatus().isBadRequest();
		webTestClient.post().uri("/bad/professional-web/journal-notes").contentType(APPLICATION_JSON).bodyValue("{}").exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/bad/professional-web/documents/1").contentType(APPLICATION_JSON).bodyValue(BODY).exchange().expectStatus().isBadRequest();

		verifyNoInteractions(serviceMock);
	}

	private String getJson(final String uri) {
		return webTestClient.get().uri(uri)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}

	private String post(final String uri) {
		return webTestClient.post().uri(uri)
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}

	private String put(final String uri) {
		return webTestClient.put().uri(uri)
			.contentType(APPLICATION_JSON)
			.bodyValue(BODY.getBytes(UTF_8))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentTypeCompatibleWith(APPLICATION_JSON)
			.expectBody(String.class).returnResult().getResponseBody();
	}
}
