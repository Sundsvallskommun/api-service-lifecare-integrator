package apptest;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/ProfessionalWebDocumentsIT/", classes = Application.class)
class ProfessionalWebDocumentsIT extends AbstractAppTest {

	@Test
	void test01_writeJournalNote() {
		setupCall()
			.withServicePath("/2281/professional-web/persons/81471222-5798-11e9-ae24-57fa13b361e1/documents")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("documents-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/services/42/journal-notes/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/journal-notes")
			.withHttpMethod(POST)
			.withRequest("create-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("create-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("edit-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21")
			.withHttpMethod(PUT)
			.withRequest("mismatched-update-request.json")
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse("mismatched-update-response.json")
			.sendRequest();

		wiremock.verify(0, postRequestedFor(urlPathEqualTo("/WESE.FC.ProfessionalWeb/api2/Document/UpdateJournalNote/")));

		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("update-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/journal-notes/21")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("view-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_writeDocument() {
		setupCall()
			.withServicePath("/2281/professional-web/services/42/documents/proposal")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("proposal-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/documents")
			.withHttpMethod(POST)
			.withRequest("create-request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponse("create-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/documents/22/edit")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("edit-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/documents/22")
			.withHttpMethod(PUT)
			.withRequest("update-request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("update-response.json")
			.sendRequest();

		setupCall()
			.withServicePath("/2281/professional-web/documents/22")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("view-response.json")
			.sendRequestAndVerifyResponse();
	}
}
