package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.lifecareintegrator.Application;

@WireMockAppTestSuite(files = "classpath:/PersonIT/", classes = Application.class)
class PersonIT extends AbstractAppTest {

	private static final String PARTY_ID = "81471222-5798-11e9-ae24-57fa13b361e1";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getPerson() {
		setupCall()
			.withServicePath("/2281/person?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getContacts() {
		setupCall()
			.withServicePath("/2281/contacts?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getPersonNotFound() {
		setupCall()
			.withServicePath("/2281/person?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
