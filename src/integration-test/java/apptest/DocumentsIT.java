package apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/DocumentsIT/", classes = Application.class)
class DocumentsIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getDocuments() {
		setupCall()
			.withServicePath("/2281/documents?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getDocumentContent() throws IOException {
		setupCall()
			.withServicePath("/2281/documents/12345/content")
			.withHttpMethod(GET)
			.withHeader("Accept", APPLICATION_PDF_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_PDF_VALUE))
			.withExpectedBinaryResponse("document.pdf")
			.sendRequestAndVerifyResponse();
	}
}
