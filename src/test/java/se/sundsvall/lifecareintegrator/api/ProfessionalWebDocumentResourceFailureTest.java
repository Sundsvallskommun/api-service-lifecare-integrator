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
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.lifecareintegrator.Application;
import se.sundsvall.lifecareintegrator.service.ProfessionalWebDocumentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
class ProfessionalWebDocumentResourceFailureTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String BODY = "{\"documentId\": 1, \"content\": \"Anteckning åäö\"}";
	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality";
	private static final String NOT_A_MUNICIPALITY_ID = "not a valid municipality ID";
	private static final String NOT_POSITIVE = "must be greater than 0";

	@MockitoBean
	private ProfessionalWebDocumentService serviceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {
		"not-a-uuid", "12345"
	})
	void getDocumentsWithAPartyIdThatIsNotAUuidIsRefused(final String partyId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/persons/" + partyId + "/documents").exchange()),
			"getDocuments.partyId", "not a valid UUID");

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getJournalNoteWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/journal-notes/" + id).exchange()), "getJournalNote.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getJournalNoteForEditWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/journal-notes/" + id + "/edit").exchange()), "getJournalNoteForEdit.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDocumentWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/documents/" + id).exchange()), "getDocument.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDocumentForEditWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/documents/" + id + "/edit").exchange()), "getDocumentForEdit.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getJournalNoteProposalWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/journal-notes/proposal").exchange()),
			"getJournalNoteProposal.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void getDocumentProposalWithServiceIdThatIsNotPositive(final String serviceId) {
		assertViolation(readViolations(webTestClient.get().uri("/2281/professional-web/services/" + serviceId + "/documents/proposal").exchange()),
			"getDocumentProposal.serviceId", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void updateJournalNoteWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.put().uri("/2281/professional-web/journal-notes/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange()),
			"updateJournalNote.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"0", "-1"
	})
	void updateDocumentWithIdThatIsNotPositive(final String id) {
		assertViolation(readViolations(webTestClient.put().uri("/2281/professional-web/documents/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange()),
			"updateDocument.id", NOT_POSITIVE);

		verifyNoInteractions(serviceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"abc", "99999999999999999999"
	})
	void idThatIsNotANumberIsRefused(final String id) {
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/journal-notes/" + id).exchange(), "id");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/journal-notes/" + id + "/edit").exchange(), "id");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/documents/" + id).exchange(), "id");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/documents/" + id + "/edit").exchange(), "id");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + id + "/journal-notes/proposal").exchange(), "serviceId");
		assertBadRequest(webTestClient.get().uri("/2281/professional-web/services/" + id + "/documents/proposal").exchange(), "serviceId");
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/journal-notes/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange(), "id");
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/documents/" + id).contentType(APPLICATION_JSON).bodyValue(BODY).exchange(), "id");

		verifyNoInteractions(serviceMock);
	}

	@Test
	void writeWithoutBodyIsRefused() {
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/journal-notes").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.post().uri("/2281/professional-web/documents").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/journal-notes/1").contentType(APPLICATION_JSON).exchange());
		assertBadRequest(webTestClient.put().uri("/2281/professional-web/documents/1").contentType(APPLICATION_JSON).exchange());

		verifyNoInteractions(serviceMock);
	}

	@Test
	void invalidMunicipalityIdIsRefused() {
		final var base = "/" + INVALID_MUNICIPALITY_ID + "/professional-web";
		assertViolation(readViolations(webTestClient.get().uri(base + "/persons/" + PARTY_ID + "/documents").exchange()), "getDocuments.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/journal-notes/1").exchange()), "getJournalNote.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/journal-notes/1/edit").exchange()), "getJournalNoteForEdit.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/documents/1").exchange()), "getDocument.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/documents/1/edit").exchange()), "getDocumentForEdit.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/services/42/journal-notes/proposal").exchange()), "getJournalNoteProposal.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.get().uri(base + "/services/42/documents/proposal").exchange()), "getDocumentProposal.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri(base + "/journal-notes").contentType(APPLICATION_JSON).bodyValue("{}").exchange()),
			"createJournalNote.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.post().uri(base + "/documents").contentType(APPLICATION_JSON).bodyValue("{}").exchange()),
			"createDocument.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.put().uri(base + "/journal-notes/1").contentType(APPLICATION_JSON).bodyValue(BODY).exchange()),
			"updateJournalNote.municipalityId", NOT_A_MUNICIPALITY_ID);
		assertViolation(readViolations(webTestClient.put().uri(base + "/documents/1").contentType(APPLICATION_JSON).bodyValue(BODY).exchange()),
			"updateDocument.municipalityId", NOT_A_MUNICIPALITY_ID);

		verifyNoInteractions(serviceMock);
	}

	@Test
	void getDocumentsForAPartyIdPartyDoesNotKnowIsNotFound() {
		when(serviceMock.getDocuments("2281", PARTY_ID)).thenThrow(Problem.valueOf(NOT_FOUND, "No person number found for partyId '" + PARTY_ID + "'"));

		webTestClient.get().uri("/2281/professional-web/persons/" + PARTY_ID + "/documents")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.detail").isEqualTo("No person number found for partyId '" + PARTY_ID + "'");

		verify(serviceMock).getDocuments("2281", PARTY_ID);
		verifyNoMoreInteractions(serviceMock);
	}

	private static ConstraintViolationProblem readViolations(final WebTestClient.ResponseSpec response) {
		return response
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();
	}

	private static void assertViolation(final ConstraintViolationProblem response, final String field, final String message) {
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple(field, message));
	}

	private static void assertBadRequest(final WebTestClient.ResponseSpec response) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request");
	}

	private static void assertBadRequest(final WebTestClient.ResponseSpec response, final String parameter) {
		response.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.title").isEqualTo("Bad Request")
			.jsonPath("$.detail").value(String.class, detail -> assertThat(detail).contains("'" + parameter + "'"));
	}
}
