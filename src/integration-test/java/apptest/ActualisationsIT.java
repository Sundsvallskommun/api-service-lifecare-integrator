package apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/ActualisationsIT/", classes = Application.class)
class ActualisationsIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getActualisations() {
		setupCall()
			.withServicePath("/2281/actualisations?partyId=" + PARTY_ID + "&from=2025-01-01&to=2026-12-31")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getActualisationProposal() {
		setupCall()
			.withServicePath("/2281/actualisations/proposal?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_createActualisation() {
		setupCall()
			.withServicePath("/2281/actualisations")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/actualisations/98765$"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_addActualisationAttachment() {
		final var fileHeaders = new HttpHeaders();
		fileHeaders.setContentType(MediaType.APPLICATION_PDF);
		fileHeaders.setContentDispositionFormData("file", "attachment.pdf");
		final var filePart = new HttpEntity<>(new ByteArrayResource("dummy-pdf-content".getBytes()) {
			@Override
			public String getFilename() {
				return "attachment.pdf";
			}
		}, fileHeaders);

		final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("documentType", "10");
		body.add("senderType", "20");
		body.add("title", "A title");
		body.add("senderName", "A sender");
		body.add("file", filePart);

		setupCall()
			.withServicePath("/2281/actualisations/98765/attachments")
			.withHttpMethod(POST)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(body)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
