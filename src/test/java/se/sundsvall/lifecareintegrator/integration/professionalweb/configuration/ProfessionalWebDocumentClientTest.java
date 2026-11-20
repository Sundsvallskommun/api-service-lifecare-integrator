package se.sundsvall.lifecareintegrator.integration.professionalweb.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.lifecareintegrator.integration.professionalweb.ProfessionalWebDocumentClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ProfessionalWebDocumentClientTest {

	private static final String DOCUMENT = "/WESE.FC.ProfessionalWeb/api2/Document/";
	private static final String LIFECARE_JSON = "{\"documentId\":1,\"content\":\"Anteckning åäö\",\"personnummer\":\"191212121212\"}";
	private static final String BODY = "{ \"documentId\": 1,\n \"content\": \"Anteckning åäö\", \"unknown\": {\"a\": [1, 2.50]} }";

	private WireMockServer wireMock;
	private ProfessionalWebDocumentClient client;

	@BeforeEach
	void setUp() {
		wireMock = new WireMockServer(options().dynamicPort());
		wireMock.start();
		client = Api2Clients.client(ProfessionalWebDocumentClient.class, wireMock.baseUrl());
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	void documentsListIsReadWithATrailingSlashAndThePersonnummerAsId() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentsListForClient/?id=191212121212")).willReturn(okJson("[]")));

		assertThat(new String(client.getDocumentsListForClient("191212121212"), UTF_8)).isEqualTo("[]");

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentsListForClient/?id=191212121212"))
			.withHeader("X-LEGACY-TOKEN", equalTo("tok")));
	}

	@Test
	void journalNoteIsReadWithIdHideRevisionsAndInEditInOrder() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetJournalNoteWithContent/?id=1&hideRevisions=true&inEdit=false")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getJournalNoteWithContent(1, true, false), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetJournalNoteWithContent/?id=1&hideRevisions=true&inEdit=false")));
	}

	@Test
	void documentIsReadWithIdHideRevisionsAndInEditInOrder() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentWithContent/?id=1&hideRevisions=false&inEdit=true")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(new String(client.getDocumentWithContent(1, false, true), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentWithContent/?id=1&hideRevisions=false&inEdit=true")));
	}

	@Test
	void proposalsAreReadWithoutATrailingSlashAndTheServiceAsId() {
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetNoteProposalForService?id=42")).willReturn(okJson("{\"note\":1}")));
		wireMock.stubFor(get(urlEqualTo(DOCUMENT + "GetDocumentProposalForService?id=42")).willReturn(okJson("{\"document\":1}")));

		assertThat(new String(client.getNoteProposalForService(42), UTF_8)).isEqualTo("{\"note\":1}");
		assertThat(new String(client.getDocumentProposalForService(42), UTF_8)).isEqualTo("{\"document\":1}");

		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetNoteProposalForService?id=42")));
		wireMock.verify(1, getRequestedFor(urlEqualTo(DOCUMENT + "GetDocumentProposalForService?id=42")));
	}

	@Test
	void createsPostTheBodyByteForByteWithATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "CreateJournalNote/")).willReturn(okJson("{\"documentId\":1}")));
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "CreateDocument/")).willReturn(okJson("{\"documentId\":2}")));

		assertThat(new String(client.createJournalNote(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"documentId\":1}");
		assertThat(new String(client.createDocument(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo("{\"documentId\":2}");

		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "CreateJournalNote/"))
			.withHeader("Content-Type", equalTo("application/json; charset=UTF-8"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "CreateDocument/"))
			.withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void updatesArePostsOfTheBodyByteForByteWithATrailingSlash() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateJournalNote/")).willReturn(aResponse().withStatus(204)));
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateDocument/")).willReturn(okJson(LIFECARE_JSON)));

		assertThat(client.updateJournalNote(BODY.getBytes(UTF_8))).isEmpty();
		assertThat(new String(client.updateDocument(BODY.getBytes(UTF_8)), UTF_8)).isEqualTo(LIFECARE_JSON);

		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "UpdateJournalNote/")).withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
		wireMock.verify(1, postRequestedFor(urlEqualTo(DOCUMENT + "UpdateDocument/")).withRequestBody(binaryEqualTo(BODY.getBytes(UTF_8))));
	}

	@Test
	void updateRefusedByLifecareIsUnprocessableWithItsText() {
		wireMock.stubFor(post(urlEqualTo(DOCUMENT + "UpdateDocument/"))
			.willReturn(aResponse().withStatus(461).withBody("{\"Message\":\"Dokumentet är låst\"}")));

		assertThatThrownBy(() -> client.updateDocument("{}".getBytes(UTF_8)))
			.asInstanceOf(type(ThrowableProblem.class))
			.extracting(problem -> problem.getStatus().value(), ThrowableProblem::getDetail)
			.containsExactly(422, "Dokumentet är låst");
	}
}
